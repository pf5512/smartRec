package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.CloudFileService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.ModuleKey.BenefitApplyState;
import com.thousandsunny.service.ModuleKey.BenefitType;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.*;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.domain.service.BaseMemberService;
import com.thousandsunny.thirdparty.model.Account;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.*;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotBlankThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.BenefitApplyState.REVIEW;
import static com.thousandsunny.service.ModuleKey.BenefitItemType.FREE_TRAIN_NORMAL;
import static com.thousandsunny.service.ModuleKey.BenefitType.*;
import static com.thousandsunny.service.ModuleKey.KeyPercentage.CONSTANT_CAR_FEE;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.EMPLOYEE_CAR_FEE;
import static java.time.LocalDate.now;
import static java.time.LocalDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.apache.commons.lang3.time.DateUtils.addMonths;
import static org.apache.commons.lang3.time.DateUtils.addYears;

@Service
public class BenefitService extends BaseService<Benefit> {

    @Autowired
    private BenefitRelRepository benefitRelRepository;
    @Autowired
    private BenefitItemRepository benefitItemRepository;
    @Autowired
    private BenefitApplyRepository benefitApplyRepository;
    @Autowired
    private BenefitRepository benefitRepository;
    @Autowired
    private BaseMemberService memberService;
    @Autowired
    private CloudFileService cloudFileRepository;
    @Autowired
    private BenefitRelService benefitRelService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private BenefitApplyDateRepository benefitApplyDateRepository;

    public BenefitRel findBenefitRel(String userToken, BenefitType type) {
        return benefitRelRepository.findByMemberTokenAndType(userToken, type);
    }

    public List<BenefitItem> findBenefitItem(String userToken) {
        Date now = new Date();
        BenefitRel benefitRel = benefitRelRepository.findByMemberTokenAndType(userToken, FREE_TRAING);
        if (benefitRel == null) return null;
        List<BenefitItem> benefitItems = benefitItemRepository.findByBenefitRelIdAndEffectiveDateLessThanAndInvalidDateGreaterThanAndState(benefitRel.getId(), now, now, ModuleKey.BenefitItemState.NORMAL);
        return benefitItems;
    }

    public BenefitApply findBenefitApply(Member member, BenefitType type) {
        return benefitApplyRepository.findByMemberIdAndTypeAndIsDelete(member.getId(), type, NO);
    }

    public void addBenefitApply(String userToken, BenefitType type, BenefitApply benefitApply, String explain) {
        Member member = memberService.findByToken(userToken);
        Benefit benefit = benefitRepository.findByType(type);
        BenefitApply oldApply;
        BenefitApply apply = benefitApplyRepository.findByMemberIdAndTypeAndIsDelete(member.getId(), type, NO);
        if (apply != null) {
            ifNotNullThen(benefitApply.getName(), x -> apply.setName(x));
            ifNotNullThen(benefitApply.getPics(), x -> apply.setPics(cloudFileRepository.save(x)));
            ifNotNullThen(benefitApply.getPhoneNumber(), x -> apply.setPhoneNumber(x));
            ifNotNullThen(benefitApply.getStoreName(), x -> apply.setStoreName(x));
            ifNotNullThen(benefitApply.getAmount(), x -> apply.setAmount(x));
            oldApply = apply;
        } else oldApply = benefitApply;
        oldApply.setApplyState(REVIEW);
        oldApply.setMember(member);
        oldApply.setType(type);
        oldApply.setValid(YES);
        oldApply.setBenefit(benefit);
        oldApply.setDate(new Date());
        oldApply.setIsTodayRemind(NO);
        oldApply.setReason(explain);
        benefitApplyRepository.save(oldApply);
    }

    public void remindApply(String userToken, BenefitType type) {
        Member member = memberService.findByToken(userToken);
        BenefitApply apply = benefitApplyRepository.findByMemberIdAndTypeAndIsDelete(member.getId(), type, NO);
        List<BenefitApplyDate> dateList = apply.getRemindDates();
        if (isNull(dateList)) dateList = newArrayList();
        dateList.add(benefitApplyDateRepository.save(new BenefitApplyDate()));
        apply.setRemindDates(dateList);
        apply.setIsTodayRemind(YES);
        benefitApplyRepository.save(apply);
    }


    public void dealBenefit() {
        List<BenefitRel> avaBenefits = benefitRelService.findTodayEffective();
        avaBenefits.stream().filter(b -> b.getType() == CAR_FEE).forEach(this::processCarFee);//处理车旅费
        avaBenefits.stream().filter(b -> b.getType() == FREE_TRAING && b.getJob().getRecType() == MONTHLY).forEach(this::processFreetraining);//免费培训
        avaBenefits.stream().filter(b -> b.getType() == WORK_INSURANCE).forEach(benefit -> {
        });//工作保险
        avaBenefits.stream().filter(b -> b.getType() == SALARY_PROTECTION).forEach(benefit -> {
        });//工资保障
        avaBenefits.stream().filter(b -> b.getType() == QUICK_LOAN).forEach(benefit -> {
        });//快速贷款
    }

    public void resetBenefitApply() {
        List<BenefitType> benefitTypes = newArrayList(SALARY_PROTECTION, QUICK_LOAN);
        List<BenefitApply> benefitApplies = benefitApplyRepository.findByApplyStateAndTypeIn(REVIEW, benefitTypes);
        ifNotNullThen(benefitApplies, x -> {
            benefitApplies.forEach(benefitApply -> benefitApply.setIsTodayRemind(NO));
            benefitApplyRepository.save(x);
        });
    }

    /**
     * 免费培训
     */
    private void processFreetraining(BenefitRel benefitRel) {
        java.util.Date startDate = truncatedDate(benefitRel.getFlow().getJobApplyRecord().getStartDate());
        Boolean valid = ofInstant(new Date(benefitRel.getInvalidDate().getTime()).toInstant(), systemDefault()).toLocalDate().isAfter(now());//是否未到失效时间
        if (valid) {//要是按照悬赏的
            List<BenefitItem> list = newArrayList();
            benefitRel.setEffectiveDate(addMonths(benefitRel.getDate(), 4));
            BenefitItem e = benefitRelService.genBenefitItemModel(benefitRel, startDate, FREE_TRAIN_NORMAL);
            list.add(e);
            ifNotNullThen(benefitRel.getBenefitItem(), x -> list.addAll(x));
            benefitRelService.save(benefitRel);
        }
    }

    /**
     * 处理车旅费
     */
    private void processCarFee(BenefitRel benefitRel) {
        Account zuesAccount = accountService.findZuesAccount();//管理员账户
        zuesAccount.setBalance(zuesAccount.getBalance().subtract(CONSTANT_CAR_FEE.val()));
        zuesAccount.setTotal(zuesAccount.getTotal().subtract(CONSTANT_CAR_FEE.val()));
        accountService.save(zuesAccount);

        Account memberAccount = accountService.findByMemberToken(benefitRel.getMember().getToken());//会员账户
        memberAccount.setBalance(memberAccount.getBalance().add(CONSTANT_CAR_FEE.val()));
        memberAccount.setTotal(memberAccount.getTotal().add(CONSTANT_CAR_FEE.val()));
        accountService.save(memberAccount);

//        Date effectiveDate = addYears(benefitRel.getEffectiveDate(), 1);
//        benefitRel.setEffectiveDate(effectiveDate);
//        Date nowDate = new Date();
//        if (yearGap(nowDate, effectiveDate) == 0 && monthGap(nowDate, effectiveDate) == 0 && dayGap(nowDate, effectiveDate) <= 1)
        benefitRel.setValid(NO);
        benefitRelService.save(benefitRel);

        accountFlowService.saveAccountFlow(benefitRel.getFlow(), CONSTANT_CAR_FEE.val(), EMPLOYEE_CAR_FEE, memberAccount, null, null);
    }


    public Page<BenefitApply> findSalaryProtection(BackPageVo backPageVo, String text, BenefitApplyState tableType, BenefitType type) {
        Specification<BenefitApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("applyState"), tableType));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            predicates.add(rb.equal(rt.get("type"), type));
            ifNotBlankThen(text, t -> rb.or(rb.like(rt.get("member").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("member").get("hpAccount"), "%" + t + "%"), rb.like(rt.get("storeName"), "%" + t + "%")));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return benefitApplyRepository.findAll(spec, backPageVo.pageRequest());
    }


    public void deleteSalaryProtection(String ids) {
        ifNotBlankThen(ids, x -> newArrayList(ids.split(",")).forEach(idStr -> {
            BenefitApply benefitApply = benefitApplyRepository.findOne(Long.parseLong(idStr));
            benefitApply.setIsDelete(YES);
            benefitApplyRepository.save(benefitApply);
        }));
    }

    public void updateSalaryProtection(Long id, BenefitApplyState status, String remark) {
        BenefitApply one = benefitApplyRepository.findOne(id);
        one.setApplyState(status);
        one.setRemark(remark);
        benefitApplyRepository.save(one);
    }

    public BenefitApply findBenefitApply(Long id) {
        return benefitApplyRepository.findOne(id);
    }

    public Page<BenefitApply> findQuickLoanPage(BackPageVo pageVo, Long userId) {
        Specification<BenefitApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member").get("id"), userId));
            predicates.add(rb.equal(rt.get("type"), QUICK_LOAN));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return benefitApplyRepository.findAll(spec, pageVo.pageRequest());
    }

    public Long countNotDealSalaryProtection(Date startTime, Date endTime) {
        return benefitApplyRepository.count(specInfo(startTime, endTime, SALARY_PROTECTION));
    }

    public Long countNotDealQuickLoan(Date startTime, Date endTime) {
        return benefitApplyRepository.count(specInfo(startTime, endTime, QUICK_LOAN));

    }

    public Specification<BenefitApply> specInfo(Date startTime, Date endTime, BenefitType type) {
        Specification<BenefitApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("applyState"), REVIEW));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            predicates.add(rb.equal(rt.get("type"), type));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return spec;
    }
}
