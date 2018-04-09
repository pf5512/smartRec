package com.thousandsunny.service.service;

import com.pingplusplus.model.Charge;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.CourseApplyState;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import com.thousandsunny.service.ModuleKey.BenefitItemState;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.ModuleKey.PayType;
import com.thousandsunny.thirdparty.ModuleKey.RecordType;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.ThirdPartyPayAccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.validation.constraints.Null;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.AccountEnum.SCHOOL;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.CourseApplyState.*;
import static com.thousandsunny.service.ModuleKey.RedPacketState.NORMAL;
import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType.COURSE_PAY;
import static com.thousandsunny.service.ModuleKey.RedPacketState.USED;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_IN;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.APPROVAL;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_OFFLINE;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.*;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.COURSE_APPLY;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_MEMBER_ACCOUNT_NOT_EXIST;
import static com.thousandsunny.thirdparty.ModuleTips.TIP_SCHOOL_ACCOUNT_NOT_EXIST;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.jackrabbit.util.Text.md5;

/**
 * Created by 13336 on 2017/2/15.
 */
@Service
public class CourseApplyService extends BaseService<CourseApply> {
    @Autowired
    private CourseApplyRepository courseApplyRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ThirdPartyPayAccountService thirdPartyPayAccountService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private SrAccountApplyRecordService accountApplyService;
    @Autowired
    private ResumeRepository resumeRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;
    @Autowired
    private ResumeTrainExpRepository resumeTrainExpRepository;
    @Autowired
    private CourseApplyGenService courseApplyGenService;
    @Autowired
    private CourseSignUpRepository courseSignUpRepository;
    @Autowired
    private RedPacketReceiveRepository redPacketReceiveRepository;
    @Autowired
    private BenefitItemRepository benefitItemRepository;

    public Page<CourseApply> findCourseApplyPage(Member member, Pageable pageable) {
        return courseApplyRepository.findByMemberIdOrderByDateDesc(member.getId(), pageable);
    }

    public CourseApply findCourseApply(Member member, String orderNo) {
        return courseApplyRepository.findBySerialNoAndMemberId(orderNo, member.getId());
    }

    public void cancelUnderLine(Member member, String orderNo) {
        CourseApply courseApply = courseApplyRepository.findBySerialNoAndMemberId(orderNo, member.getId());
        ifFalseThrow(courseApply.getState() == COURSE_ORDER_OFFLINE_PAY_CONFIRM, TIP_COURSE_CAN_NOT_CANCEL_UNDER_LINE);
        courseApply.setState(COURSE_ORDER_WAIT_FOR_PAY);
//      将产生的线下付款待后台确认的流水删除
        AccountFlow accountFlow = accountFlowService.findCourseApplyFlow(courseApply, APPROVAL);
        if (isNotNull(accountFlow))
            accountFlowService.delete(accountFlow);
        courseApplyRepository.save(courseApply);
    }

    public void cancelCourse(Member member, String orderNo) {
        CourseApply courseApply = courseApplyRepository.findBySerialNoAndMemberId(orderNo, member.getId());
        ifFalseThrow(courseApply.getState() == COURSE_ORDER_WAIT_FOR_PAY, TIP_COURSE_CAN_NOT_CANCEL_TRAIN);
        courseApply.setState(COURSE_ORDER_CLOSED);
        courseApply.setCloseDate(new Date());
        courseApplyRepository.save(courseApply);
    }

    public void finishCourse(Member member, String orderNo) {
        CourseApply courseApply = courseApplyRepository.findBySerialNoAndMemberId(orderNo, member.getId());
        //type= COURSE_ORDER_PAID(已付款) 且当前时间 > 培训时间+培训天数，才允许发起培训完成操作
        Boolean flag = courseApply.getState() == COURSE_ORDER_PAID &&
                (new Date().getTime() >= (courseApply.getTrainDate().getTime() + (courseApply.getCourse().getDay() * (24 * 3600 * 1000))));
        ifFalseThrow(flag, TIP_COURSE_CAN_NOT_FINISH_TRAIN);
        courseApply.setState(COURSE_ORDER_TRAINED_UNCOMMENT);
        courseApplyRepository.save(courseApply);
        //将学校账户中冻结的钱给解冻
        if (courseApply.getIsUseFee() == NO) {
            Account schoolAccount = accountService.findByMemberToken(courseApply.getCourse().getSchool().getMember().getToken());
            ifNullThrow(schoolAccount, TIP_SCHOOL_ACCOUNT_NOT_EXIST);
            BigDecimal money = courseApply.getPrice().subtract(courseApply.getDiscount());//用户付的钱
            BigDecimal schoolMoney = money.multiply(new BigDecimal((1d - (courseApply.getCourse().getPlatformPercent() / 100) + "")));//学校分的钱
            schoolAccount.setFreezingAmount(schoolAccount.getFreezingAmount().subtract(schoolMoney));
            accountService.save(schoolAccount);
        }
    }

    public CourseApply saveCourseApply(CourseApply courseApply, Member member, Long courseId, Date date) {
        Course course = courseRepository.findOne(courseId);
        ifNullThrow(course, TIP_NO_COURSE);
        courseApply.setTrainDate(date);
        courseApply.setDate(new Date());
        courseApply.setCourse(course);
        courseApply.setMember(member);
        courseApply.setState(COURSE_ORDER_WAIT_FOR_PAY);
        if (courseApply.getBenefitItem() != null && courseApply.getBenefitItem().getId() != null) {
            //免费培训
            courseApply.setIsUseFee(YES);
            courseApply.setPrice(course.getPrice());
            courseApply.setDiscount(course.getPrice());
            BenefitItem benefitItem = benefitItemRepository.findOne(courseApply.getBenefitItem().getId());
            ifNullThrow(benefitItem, TIP_NO_BENEFITREL);
            benefitItem.setState(BenefitItemState.USED);
            benefitItemRepository.save(benefitItem);
        } else {
            courseApply.setPrice(course.getPrice());
            courseApply.setIsUseFee(NO);
        }
        final BigDecimal[] discount = {BigDecimal.ZERO};//红包总金额
        List<RedPacketReceive> list = new ArrayList<>();
        ifNotNullThen(courseApply.getRedPacketReceives(), x -> x.forEach(redPacketReceive -> {
            RedPacketReceive receive = redPacketReceiveRepository.findByIdAndState(redPacketReceive.getId(), NORMAL);
            ifNullThrow(receive, TIP_NO_REDPACKETRECEIVE);
            receive.setState(USED);
            list.add(redPacketReceiveRepository.save(receive));
            discount[0] = discount[0].add(receive.getRedPacket().getAmount());
        }));
        courseApply.setRedPacketReceives(list);
        BigDecimal redPacket = course.getPrice().multiply(new BigDecimal(course.getRedPacketPercent() / 100 + ""));
        //如果红包总金额大于红包比例,那么就最多打折红包比例金额
        if (discount[0].compareTo(redPacket) > 0) {
            courseApply.setDiscount(redPacket);
        } else courseApply.setDiscount(discount[0]);
        courseApply.setSchool(course.getSchool());
        CourseApplyGen courseApplyGen = courseApplyGenService.getMaxNo();
        courseApply.setSerialNo(prefix() + courseApplyGen.getSeq());
        courseApply = courseApplyRepository.save(courseApply);

        courseApplyGen.setCourseApply(courseApply);
        courseApplyGenService.save(courseApplyGen);
        return courseApply;
    }

    private static String prefix() {
        return "A" + format(new Date(), "yyMMdd");
    }

    public Charge pay(Member member, String orderNo, PayType payType, String payPassword, String openId) {
        CourseApply courseApply = courseApplyRepository.findBySerialNo(orderNo);
        ifNullThrow(courseApply, TIP_NO_COURSEAPPLY);
        ifFalseThrow(member.getId().equals(courseApply.getMember().getId()), TIP_NO_AUTHORITY);
        ifFalseThrow(courseApply.getState() == COURSE_ORDER_WAIT_FOR_PAY, TIP_NO_AUTHORITY);
        if (courseApply.getIsUseFee() == YES) {
            courseApply.setState(COURSE_ORDER_PAID);
            courseApplyRepository.save(courseApply);
            return null;
        }
        BigDecimal money = courseApply.getPrice().subtract(courseApply.getDiscount());//应该支付的钱
        if (payType == PayType.PAY_BY_BALANCE) {
            Account memberAccount = accountService.findByMemberToken(member.getToken());
            ifNullThrow(memberAccount, TIP_MEMBER_ACCOUNT_NOT_EXIST);
            ifTrueThrow(memberAccount.getBalance().compareTo(money) < 0, TIP_NO_ENOUGH_BALANCE);
            ifFalseThrow(memberAccount.getPayPassword().equals(md5(md5(payPassword))), TIP_ERROR_PAY_PASSWORD);
            //个人账户出钱
//            memberAccount.setBalance(memberAccount.getBalance().subtract(money));
//            memberAccount.setTotal(memberAccount.getTotal().subtract(money));
//            accountService.save(memberAccount);
            //个人账户 -> 平台
            accountService.memberAccountPayMoney(memberAccount, money);
            createMemberAccountFlow(courseApply, money, payType, memberAccount, BILL_PAY_ONLINE, PAY_OUT);//个人账户出账流水
            divideMoney(courseApply, money, payType);
            courseApply.setState(COURSE_ORDER_PAID);
            courseApplyRepository.save(courseApply);
            CourseSignUp courseSignUp = courseSignUpRepository.
                    findByCourseIdAndDateAndIsDeleteAndIsEnable(courseApply.getCourse().getId(),
                            new java.sql.Date(courseApply.getTrainDate().getTime()), NO, YES);
            ifNullThrow(courseSignUp, TIP_NO_COURSE_SIGN_UP);
            ifTrueThrow(courseSignUp.getCount() - courseSignUp.getSignedCount() <= 0, TIP_CAN_NOT_SIGN_UP);
            courseSignUp.setSignedCount(courseSignUp.getSignedCount() + 1);
            courseSignUpRepository.save(courseSignUp);
            return null;
        }
        Charge charge = thirdPartyPayAccountService.choosePayType(member.getToken(), payType, money, COURSE_APPLY, null, null, null, null, courseApply, "课程报名付款", openId);//付款
        if (payType == PayType.PAY_BY_WX || payType == PayType.PAY_BY_ALIPAY || payType == PayType.PAY_BY_WX_PUB) {
            SrAccountApplyRecord accountApplyRecord = accountApplyService.findByOrderNo(charge.getOrderNo());
            accountApplyRecord.setCourseApply(courseApply);
            accountApplyRecord.setAmount(money);
            accountApplyRecord.setSource(COURSE_PAY);
            accountApplyService.save(accountApplyRecord);
        }
        if (payType == PayType.PAY_OFFLINE) {
            courseApply.setState(COURSE_ORDER_OFFLINE_PAY_CONFIRM);
        }
        courseApply.setPayType(payType);
        courseApply.setPayDate(new Date());
        courseApplyRepository.save(courseApply);
        return charge;
    }

    /**
     * 平台账户，学校账户分钱逻辑
     * 用户付的钱 = 课程费用 - 红包
     * 学校分的钱 = 课程费用*（1-平台分钱比例）
     * 平台分的钱 = 用户付的钱 - 学校分的钱
     *
     * @Author mu.jie
     * @Date 2017/2/16
     */
    public void divideMoney(CourseApply courseApply, BigDecimal money, PayType payType) {
        Course course = courseApply.getCourse();
        ifNullThrow(course, TIP_NO_COURSE);
        Double platformPercent = course.getPlatformPercent();
        BigDecimal schoolMoney = money.multiply(new BigDecimal((1D - (platformPercent / 100)) + ""));//学校应该分的钱
        BigDecimal platformMoney = money.subtract(schoolMoney);//平台应该分的钱

        Account platformAccount = accountService.findZuesAccount();//平台账户
        //学校账户进钱
        Account schoolAccount = accountService.findByMemberToken(course.getSchool().getMember().getToken());
        ifNullThrow(schoolAccount, TIP_SCHOOL_ACCOUNT_NOT_EXIST);
        accountService.freezeZuesAccountRefundMoney(schoolAccount, schoolMoney, "课程报名");//平台 -> 学校
        createMemberAccountFlow(courseApply, schoolMoney, payType, schoolAccount, BILL_INCOME, PAY_IN);//学校账户进账流水
        createPlatformAccountFlow(courseApply, schoolMoney, payType, PLATFORM_OUT, platformAccount, PAY_OUT);//平台账户出账流水

        createPlatformAccountFlow(courseApply, platformMoney, payType, PLATFORM_INCOME, platformAccount, PAY_IN);//平台进账收益流水
    }

    private AccountFlow createMemberAccountFlow(CourseApply courseApply, BigDecimal money, PayType payType, Account account, RecordType recordType, ModuleKey.ChargeType type) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setCourseApply(courseApply);
        accountFlow.setAccount(account);
        accountFlow.setAmount(money);
        accountFlow.setType(type);
        accountFlow.setRecordType(recordType);
        accountFlow.setSource(COURSE_APPLY);
        accountFlow.setPayType(payType);
        accountFlow.setState(SUCCESS);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRemark(COURSE_APPLY.getRemark());
        return accountFlowService.save(accountFlow);
    }

    private AccountFlow createPlatformAccountFlow(CourseApply courseApply, BigDecimal money, PayType payType, RecordType recordType, Account platformAccount, ModuleKey.ChargeType type) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setCourseApply(courseApply);
        accountFlow.setAccount(platformAccount);
        accountFlow.setAmount(money);
        accountFlow.setType(type);
        accountFlow.setRecordType(recordType);
        accountFlow.setSource(COURSE_APPLY);
        accountFlow.setPayType(payType);
        if (payType == payType.PAY_BY_BALANCE) {
            accountFlow.setState(SUCCESS);
        } else {
            accountFlow.setState(APPROVAL);
        }
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRemark(COURSE_APPLY.getRemark());
        return accountFlowService.save(accountFlow);
    }

    /**
     * 课程申请确认线下付款
     *
     * @Author mu.jie
     * @Date 2017/2/22
     */
    public void confirmCourseApply(CourseApply courseApply) {
        courseApply.setState(COURSE_ORDER_PAID);
        BigDecimal money = courseApply.getPrice().subtract(courseApply.getDiscount());
        CourseSignUp courseSignUp = courseSignUpRepository.
                findByCourseIdAndDateAndIsDeleteAndIsEnable(courseApply.getCourse().getId(),
                        new java.sql.Date(courseApply.getTrainDate().getTime()), NO, YES);
        ifNullThrow(courseSignUp, TIP_NO_COURSE_SIGN_UP);
        ifTrueThrow(courseSignUp.getCount() - courseSignUp.getSignedCount() <= 0, TIP_CAN_NOT_SIGN_UP);
        courseSignUp.setSignedCount(courseSignUp.getSignedCount() + 1);
        courseSignUpRepository.save(courseSignUp);
        //学校和平台分钱,个人账户不减钱
        Account platform = accountService.findZuesAccount();
        platform.setBalance(platform.getBalance().add(money));
        platform.setTotal(platform.getTotal().add(money));
        accountService.save(platform);
        divideMoney(courseApply, money, PAY_OFFLINE);
        courseApplyRepository.save(courseApply);
    }

    /**
     * 后台3.4.1
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public Page<CourseApply> findAllCourseApplyPage(Pageable pageable, String text, CourseApplyState orderStatus, Member member) {
        Specification<CourseApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if (member.getRole() == SCHOOL)
                predicates.add(rb.equal(rt.get("school").get("member").get("id"), member.getId()));
            ifNotBlankThen(text, e -> predicates.add(rb.or(rb.like(rt.get("member").get("realName"), "%" + e + "%"),
                    rb.like(rt.get("member").get("mobile"), "%" + e + "%"), rb.like(rt.get("serialNo"), "%" + e + "%"))));
            ifNotNullThen(orderStatus, e -> predicates.add(rb.equal(rt.get("state"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return courseApplyRepository.findAll(spec, pageable);
    }

    public Page<CourseApply> findMyCourseApplyPage(Pageable pageable, CourseApplyState orderStatus, Long userId) {
        Specification<CourseApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member").get("id"), userId));
            ifNotNullThen(orderStatus, e -> predicates.add(rb.equal(rt.get("state"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return courseApplyRepository.findAll(spec, pageable);
    }

    /**
     * 后台3.4.3
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public void uploadCertificate(Long id, String imgs) {
        CourseApply courseApply = courseApplyRepository.findOne(id);
        ifTrueThrow(courseApply.getState() != COURSE_ORDER_TRAINED_UNCOMMENT && courseApply.getState() != COURSE_ORDER_TRAINED_COMMENTED, TIP_CAN_NOT_UPLOAD_IMG);
        String[] imgPath = imgs.split(",");
        //保存培训经历
        Resume resume = resumeRepository.findByMemberToken(courseApply.getMember().getToken());
        if (!isNotNull(resume)) {
            resume = new Resume();
            resume.setMember(courseApply.getMember());
        }
        List<CloudFile> list = new ArrayList<>();
        for (int i = 0; i < imgPath.length; i++) {
            CloudFile cloudFile = new CloudFile();
            cloudFile.setPath(imgPath[i]);
            cloudFile.setIsPlatformAdd(YES);
            list.add(cloudFileRepository.save(cloudFile));
        }
        ResumeTrainExp resumeTrainExp;
        resumeTrainExp = resumeTrainExpRepository.
                findByResumeMemberIdAndCourseIdAndIsPlatformAdd(courseApply.getMember().getId(), courseApply.getCourse().getId(), YES);
        if (!isNotNull(resumeTrainExp)) {
            resumeTrainExp = new ResumeTrainExp();
            resumeTrainExp.setCourse(courseApply.getCourse());
            resumeTrainExp.setCourseName(courseApply.getCourse().getName());
            resumeTrainExp.setInstitutionName(courseApply.getSchool().getName());
            resumeTrainExp.setIsPlatformAdd(YES);
            resumeTrainExp.setResume(resumeRepository.save(resume));
            resumeTrainExp.setStartDate(courseApply.getTrainDate());
            resumeTrainExp.setEndDate(addDays(courseApply.getTrainDate(), courseApply.getCourse().getDay()));
        }
        resumeTrainExp.setCertification(list);
        resumeTrainExpRepository.save(resumeTrainExp);
    }

    private Date addDays(Date trainDate, Integer day) {
        Long dateTime = trainDate.getTime();
        return new Date(dateTime + (day * (24 * 3600 * 1000)));
    }

    /**
     * 后台3.4.4 获取培训订单的证书列表
     *
     * @Author xiao xue wei
     * @Date 2017/2/16
     */
    public List<CloudFile> findCertificateList(Long id) {
        CourseApply courseApply = findOne(id);
        ifTrueThrow(isNull(courseApply) && courseApply.getState() != COURSE_ORDER_TRAINED_UNCOMMENT && courseApply.getState() != COURSE_ORDER_TRAINED_COMMENTED, TIP_NO_CERTIFICATE);
        ResumeTrainExp resumeTrainExp = resumeTrainExpRepository.
                findByResumeMemberIdAndCourseIdAndIsPlatformAdd(courseApply.getMember().getId(), courseApply.getCourse().getId(), YES);
        if (!isNotNull(resumeTrainExp)) return new ArrayList<>();
        return resumeTrainExp.getCertification();
    }

    public void courseOrderRemark(Long id, String remark) {
        CourseApply courseApply = courseApplyRepository.findOne(id);
        ifNullThrow(courseApply, TIP_NO_COURSEAPPLY);
        ifNotNullThen(remark, e -> courseApply.setRemark(e));
        courseApplyRepository.save(courseApply);
    }

    public CourseApply findRedPacketCourseApply(RedPacketReceive redPacketReceive) {
        List<CourseApply> courseApplies = courseApplyRepository.findByMember(redPacketReceive.getMember());
        if (!courseApplies.isEmpty()) {
            for (CourseApply courseApply : courseApplies) {
                List<RedPacketReceive> list = courseApply.getRedPacketReceives();
                if (!list.isEmpty()) {
                    for (RedPacketReceive receive : list) {
                        if (redPacketReceive.getId().equals(receive.getId())) return courseApply;
                    }
                }
            }
        }
        return null;
    }
}
