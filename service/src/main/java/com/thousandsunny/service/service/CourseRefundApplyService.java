package com.thousandsunny.service.service;

import com.pingplusplus.model.Charge;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.CourseRefundReason;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.ThirdPartyPayAccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.AccountEnum.SCHOOL;
import static com.thousandsunny.service.ModuleKey.BenefitItemType.FREE_TRAIN_NORMAL;
import static com.thousandsunny.service.ModuleKey.BenefitType.FREE_TRAING;
import static com.thousandsunny.service.ModuleKey.CourseApplyState.*;
import static com.thousandsunny.service.ModuleKey.CourseRefundReason.HAVE_FEE_TRAING;
import static com.thousandsunny.service.ModuleKey.CourseRefundWay.SCHOOL_BALANCE;
import static com.thousandsunny.service.ModuleKey.CourseRefundWay.SCHOOL_BALANCE_WITHDRAW;
import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType.SCHOOL_PAY;
import static com.thousandsunny.service.ModuleKey.BenefitItemState.NORMAL;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.*;
import static com.thousandsunny.thirdparty.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.COURSE_REFUND;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.SCHOOL_OF_PAY;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_REFUND;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateUtils.addMonths;

/**
 * Created by mu.jie on 2017/2/17.
 */
@Service
public class CourseRefundApplyService extends BaseService<CourseRefundApply> {
    @Autowired
    private CourseRefundApplyRepository courseRefundApplyRepository;
    @Autowired
    private CourseApplyRepository courseApplyRepository;
    @Autowired
    private BenefitRelRepository benefitRelRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private BenefitItemRepository benefitItemRepository;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private ThirdPartyPayAccountService thirdPartyPayAccountService;
    @Autowired
    private SrAccountApplyRecordService srAccountApplyRecordService;

    /**
     * 1、退款条件：合作课程申请退款 判断用户帐号上的是否存在免费培训的机会，
     * 如果有，判断是“职业规划”还是“免费培训”，仅在有“免费培训”的情况下，
     * 退款才能申请“我有免费培训了，我要申请退款”；“职业规划” 仅能用培训“职业规划”课程当中；
     * 自培训完成后4个月内，可申请退款，超过时间，则不显示“退款”按钮；
     * 2、退款只能申请一次
     * type= COURSE_ORDER_PAID ，或 （type=COURSE_ORDER_TRAINED且培训完成后4个月内），允许发起退款操作
     *
     * @Author mu.jie
     * @Date 2017/2/17
     */
    public void refund(Member member, String orderNo, CourseRefundReason reason, String remark, BigDecimal amount) {
        Date date = new Date();
        CourseApply courseApply = courseApplyRepository.findBySerialNo(orderNo);
        ifNullThrow(courseApply, TIP_NO_COURSEAPPLY);
        CourseRefundApply courseRefundApply = courseRefundApplyRepository.findByMemberAndCourseApply(member, courseApply);
        ifNotNullThrow(courseRefundApply, TIP_HAS_APPLYED);
        ifTrueThrow(courseApply.getPrice().subtract(courseApply.getDiscount()).compareTo(amount) < 0, TIP_REFUND_AMOUNT_ERROR);
        //如果是免费培训申请
        if (reason == HAVE_FEE_TRAING) {
            BenefitRel benefitRel = benefitRelRepository.findByMemberTokenAndType(member.getToken(), FREE_TRAING);
            if (isNotNull(benefitRel)) {
                List<BenefitItem> benefitItem = benefitItemRepository.findByBenefitRelIdAndEffectiveDateLessThanAndInvalidDateGreaterThanAndState(benefitRel.getId(), date, date, NORMAL);
                if (benefitItem != null && benefitItem.size() > 0) {
                    benefitItem.stream().
                            filter(item -> date.compareTo(item.getEffectiveDate()) > 0 && date.compareTo(item.getInvalidDate()) < 0)
                            .forEach(item -> {
                                if (item.getType() == FREE_TRAIN_NORMAL)
                                    createCourseRefundApply(courseApply, member, reason, remark, amount);
                            });
                    return;
                }
            }
            ifFalseThrow(false, TIP_CAN_NOT_REFUND);
        } else {
            //type= COURSE_ORDER_PAID ，或 （type=COURSE_ORDER_TRAINED且培训完成后4个月内），允许发起退款操作
            Boolean falg1 = courseApply.getState() == COURSE_ORDER_PAID && date.compareTo(courseApply.getTrainDate()) > 0;
            Date traingEndDate = addMonths(courseApply.getTrainDate(), courseApply.getCourse().getDay());
            Boolean falg2 = (courseApply.getState() == COURSE_ORDER_TRAINED_UNCOMMENT || courseApply.getState() == COURSE_ORDER_TRAINED_COMMENTED)
                    && addMonths(date, 4).compareTo(traingEndDate) > 0;
            if (falg1 || falg2) {
                createCourseRefundApply(courseApply, member, reason, remark, amount);
            }
        }
        courseApply.setState(COURSE_ORDER_REFUNDING);
        courseApplyRepository.save(courseApply);
    }

    private CourseRefundApply createCourseRefundApply(CourseApply courseApply, Member member, CourseRefundReason reason, String remark, BigDecimal amount) {
        CourseRefundApply courseRefundApply = new CourseRefundApply();
        courseRefundApply.setCourseApply(courseApply);
        courseRefundApply.setMember(member);
        courseRefundApply.setRemark(remark);
        courseRefundApply.setReason(reason);
        courseRefundApply.setAmount(amount);
        if (courseApply.getState() == COURSE_ORDER_PAID) {
            courseRefundApply.setWay(SCHOOL_BALANCE);
        } else if (courseApply.getState() == COURSE_ORDER_TRAINED_UNCOMMENT || courseApply.getState() == COURSE_ORDER_TRAINED_COMMENTED) {
            courseRefundApply.setWay(SCHOOL_BALANCE_WITHDRAW);
        }
        BigDecimal schoolMoney = amount.multiply(new BigDecimal((1D - (courseApply.getCourse().getPlatformPercent() / 100)) + ""));//学校分到的钱
        BigDecimal platformMoney = amount.subtract(schoolMoney);//平台分到的钱
        courseRefundApply.setPlatform(platformMoney);
        courseRefundApply.setSchool(schoolMoney);
        courseRefundApply.setState(COURSE_ORDER_REFUNDING);
        Account memberAccount = accountService.findByMemberToken(member.getToken());
        ifNullThrow(memberAccount, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        //创建一条个人的退款收入流水，本来是在后台创建审核通过是创建，现在改成在这创建，然后在后台审核时更改流水的状态
        accountFlowService.processCourseRefundAccountFlow(courseApply, courseRefundApply, memberAccount, schoolMoney, ModuleKey.ChargeType.PAY_IN);
        return courseRefundApplyRepository.save(courseRefundApply);
    }

    public void cancelCourseRefund(Member member, String orderNo) {
        CourseApply courseApply = courseApplyRepository.findBySerialNo(orderNo);
        ifNullThrow(courseApply, TIP_NO_COURSEAPPLY);
        CourseRefundApply courseRefundApply = courseRefundApplyRepository.findByMemberAndCourseApply(member, courseApply);
        ifNullThrow(courseRefundApply, TIP_REFUND_APPLY_NOT_EXIST);
        ifTrueThrow(courseRefundApply.getState() != COURSE_ORDER_REFUNDING, TIP_COURSE_REFUND_STATE_ERROR);
        courseRefundApply.setState(COURSE_ORDER_REFUND_CANCEL);
        courseRefundApply.setCancelTime(new Date());
        courseRefundApplyRepository.save(courseRefundApply);
        courseApply.setState(COURSE_ORDER_REFUND_CANCEL);
        courseApplyRepository.save(courseApply);
    }

    public CourseRefundApply findByMemberAndCourseApply(Member member, CourseApply courseApply) {
        return courseRefundApplyRepository.findByMemberAndCourseApply(member, courseApply);
    }


    public Page<CourseRefundApply> findRefundList(Member member, BackPageVo backPageVo, String tableType, String text, Date startTime, Date endTime) {
        School school = schoolRepository.findByMemberId(member.getId());
        Specification<CourseRefundApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if ("APPROVAL".equals(tableType)) {
                predicates.add(rb.equal(rt.get("state"), COURSE_ORDER_REFUNDING));
            } else if ("SUCCESS".equals(tableType)) {
                predicates.add(rb.or(rb.equal(rt.get("state"), COURSE_ORDER_REFUNDED), rb.equal(rt.get("state"), COURSE_ORDER_REFUND_FAIL)));
            }
            if (member.getRole() == SCHOOL) {
                ifNullThrow(school, TIP_NO_SCHOOL);
                predicates.add(rb.equal(rt.get("courseApply").get("school"), school));
            }
            ifNotBlankThen(text, t -> predicates.add(rb.or(rb.like(rt.get("member").get("mobile"), "%" + t + "%"),
                    rb.like(rt.get("member").get("realName"), "%" + t + "%"), rb.like(rt.get("courseApply").get("serialNo"), "%" + t + "%"))));
            ifNotNullThen(startTime, t -> predicates.add(rb.greaterThan(rt.get("createTime"), t)));
            ifNotNullThen(endTime, t -> predicates.add(rb.lessThan(rt.get("createTime"), t)));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return courseRefundApplyRepository.findAll(spec, backPageVo.pageRequest());
    }

    public BigDecimal countRefund(Member member, String text, Date startTime, Date endTime, String type) {
        School school = schoolRepository.findByMemberId(member.getId());
        StringBuffer sql = new StringBuffer("select ");
        if ("school".equals(type)) {
            sql.append(" SUM(courserefu0_.school)");
        } else if ("platform".equals(type)) {
            sql.append(" SUM(courserefu0_.platform)");
        } else {
            sql.append(" SUM(courserefu0_.amount)");
        }
        sql.append(" from sr_course_refund_apply courserefu0_" +
                " cross join core_member member1_ " +
                " cross join  sr_course_apply courseappl3_ " +
                " where courserefu0_.member_id=member1_.id  and courserefu0_.course_apply_id=courseappl3_.id ");
        sql.append("and ( courserefu0_.state='COURSE_ORDER_REFUNDED' or courserefu0_.state='COURSE_ORDER_REFUND_FAIL' )");
        if (isNotBlank(text)) {
            sql.append(" and ( member1_.mobile like '%" + text + "%' " +
                    " or member1_.real_name like '%" + text + "%' " +
                    " or courseappl3_.serial_no like '%" + text + "%' )");
        }
        if (member.getRole() == SCHOOL) {
            sql.append(" and courseappl3_.school_id=" + school.getId());
        }
        if (startTime != null) {
            sql.append(" and courserefu0_.create_time>'" + new java.sql.Date(startTime.getTime()) + "' ");
        }
        if (endTime != null) {
            sql.append(" and courserefu0_.create_time<'" + new java.sql.Date(endTime.getTime()) + "' ");
        }
        Query query = entityManager.createNativeQuery(sql.toString());
        BigDecimal singleResult = (BigDecimal) query.getSingleResult();
        return singleResult;
    }

    /**
     * 退款，将平台/学校 得到的钱退还给用户
     *
     * @Author mu.jie
     * @Date 2017/2/22
     */
    public void refundPay(CourseRefundApply courseRefundApply) {
        BigDecimal money = courseRefundApply.getAmount();//申请退款总金额
        CourseApply courseApply = courseRefundApply.getCourseApply();
        BigDecimal schoolMoney = money.multiply(new BigDecimal(1d - (courseApply.getCourse().getPlatformPercent() / 100) + ""));//学校分的钱
        BigDecimal platformMoney = money.subtract(schoolMoney);//平台分的钱
        School school = courseApply.getSchool();
        ifNullThrow(school, TIP_NO_SCHOOL);
        Account schoolAccount = accountService.findByMemberToken(school.getMember().getToken());//学校账户
        Account platformAccount = accountService.findZuesAccount();//平台账户
        Account memberAccount = accountService.findByMemberToken(courseRefundApply.getMember().getToken());//个人账户
        ifNullThrow(schoolAccount, TIP_SCHOOL_ACCOUNT_NOT_EXIST);
        ifNullThrow(memberAccount, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        ifTrueThrow(schoolMoney.compareTo(schoolAccount.getBalance()) > 0, TIPS_SCHOOL_ACCOUNT_BALANCE_ENOUGH);
        platformAccount.setBalance(platformAccount.getBalance().subtract(platformMoney));
        platformAccount.setTotal(platformAccount.getTotal().subtract(platformMoney));
        //平台出账流水
        accountFlowService.savePlatformFlow(null, null, platformAccount, platformMoney, COURSE_REFUND, BILL_REFUND, courseApply, courseRefundApply, null);

        schoolAccount.setBalance(schoolAccount.getBalance().subtract(schoolMoney));
        schoolAccount.setTotal(schoolAccount.getTotal().subtract(schoolMoney));
        //学校出账流水
        accountFlowService.processCourseRefundAccountFlow(courseApply, courseRefundApply, schoolAccount, schoolMoney, ModuleKey.ChargeType.PAY_OUT);

        memberAccount.setBalance(memberAccount.getBalance().add(schoolMoney).add(platformMoney));
        memberAccount.setTotal(memberAccount.getTotal().add(schoolMoney).add(platformMoney));
        //个人进账流水
//        accountFlowService.processCourseRefundAccountFlow(courseApply, courseRefundApply, memberAccount, schoolMoney, ModuleKey.ChargeType.PAY_IN);
        AccountFlow accountFlow = accountFlowService.findCourseRefundApplyFlow(courseApply, courseRefundApply, ModuleKey.FlowState.APPROVAL);
        ifNotNullThen(accountFlow, x -> {
            x.setState(ModuleKey.FlowState.SUCCESS);
            accountFlowService.save(accountFlow);
        });
        accountService.save(platformAccount);
        accountService.save(schoolAccount);
        accountService.save(memberAccount);

        courseRefundApply.setState(COURSE_ORDER_REFUNDED);
        save(courseRefundApply);
    }

    public CourseRefundApply findCourseRefundApply(CourseApply courseApply) {
        return courseRefundApplyRepository.findByMemberAndCourseApply(courseApply.getMember(), courseApply);
    }

    public Charge pay(CourseRefundApply courseRefundApply) {
        BigDecimal money = courseRefundApply.getAmount();//申请退款总金额
        CourseApply courseApply = courseRefundApply.getCourseApply();
        BigDecimal schoolMoney = money.multiply(new BigDecimal(1d - (courseApply.getCourse().getPlatformPercent() / 100) + ""));//学校分的钱
        School school = courseApply.getSchool();
        ifNullThrow(school, TIP_NO_SCHOOL);
        Account schoolAccount = accountService.findByMemberToken(school.getMember().getToken());//学校账户
        if (schoolMoney.compareTo(schoolAccount.getBalance()) > 0) {
            Charge charge = thirdPartyPayAccountService.choosePayType(school.getMember().getToken(), ALIPAY_PC_DIRECT, schoolMoney.subtract(schoolAccount.getBalance()), SCHOOL_OF_PAY, null, null, null, null, null, "学校付款", null);
            SrAccountApplyRecord accountApplyRecord = srAccountApplyRecordService.findByOrderNo(charge.getOrderNo());
            accountApplyRecord.setCourseRefundApply(courseRefundApply);
            accountApplyRecord.setAmount(schoolMoney);
            accountApplyRecord.setSource(SCHOOL_PAY);
            srAccountApplyRecordService.save(accountApplyRecord);
            return charge;
        }
        return null;
    }
}
