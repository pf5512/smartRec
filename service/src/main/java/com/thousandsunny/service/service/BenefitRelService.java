package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.Benefit;
import com.thousandsunny.service.model.BenefitItem;
import com.thousandsunny.service.model.BenefitRel;
import com.thousandsunny.service.model.JobApplyRecord;
import com.thousandsunny.service.repository.BenefitItemRepository;
import com.thousandsunny.service.repository.BenefitRelRepository;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.truncatedDate;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.BenefitItemState.NORMAL;
import static com.thousandsunny.service.ModuleKey.*;
import static com.thousandsunny.service.ModuleKey.BenefitItemType.FREE_TRAIN_EMPLOYMENT_PLANNING;
import static com.thousandsunny.service.ModuleKey.BenefitItemType.FREE_TRAIN_NORMAL;
import static com.thousandsunny.service.ModuleKey.BenefitType.CAR_FEE;
import static com.thousandsunny.service.ModuleKey.BenefitType.FREE_TRAING;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.ONCE;
import static com.thousandsunny.service.ModuleTips.TIP_NO_BENEFITREL;
import static java.sql.Date.valueOf;
import static java.time.LocalDate.now;
import static org.apache.commons.lang3.time.DateUtils.addMonths;
import static org.apache.commons.lang3.time.DateUtils.addYears;
import static com.thousandsunny.service.ModuleKey.BenefitType.SALARY_PROTECTION;

/**
 * 如果这些代码有用，那它们是guitarist在01/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class BenefitRelService extends BaseService<BenefitRel> {
    @Autowired
    private BenefitRelRepository benefitRelRepository;
    @Autowired
    private BenefitItemRepository benefitItemRepository;

    /**
     * 现在可用的好处
     */
    public List<BenefitRel> findTodayEffective() {
        return benefitRelRepository.findByEffectiveDateLessThanAndValidAndTypeIn(valueOf(now()), YES, newArrayList(CAR_FEE, FREE_TRAING));
    }

    public BenefitRel findByMemberTokenAndJobIdAndType(String token, Long id, BenefitType carFee) {
        return benefitRelRepository.findByMemberTokenAndJobIdAndType(token, id, carFee);
    }

    public List<BenefitRel> findByMemberToken(String token) {
        return benefitRelRepository.findByMemberTokenAndInvalidDateIsNullOrInvalidDateLessThan(token, valueOf(now()));
    }

    /**
     * (valid==NO)从来没有工作过 || invalidDate<now())
     */
    public BenefitRel carFeeIsValid(String token) {
        Specification<BenefitRel> specification = (root, query, cb) -> cb.and(
                cb.and(
                        cb.equal(root.get("member").get("token"), token),
                        cb.equal(root.get("type"), CAR_FEE)),
                cb.or(
//                        cb.equal(root.get("valid"), NO),
                        root.get("invalidDate").isNull(),
                        cb.lessThan(root.get("invalidDate"), new Date()))
        );
        return benefitRelRepository.findOne(specification);
    }

    /**
     * 新增好处
     */
    public void createBenefitRel(AccountFlow accountFlow, Benefit benefit, JobApplyRecord jobApplyRecord) {
        BenefitRel benefitRel = new BenefitRel();
        benefitRel.setType(benefit.getType());
        benefitRel.setValid(NO);
        benefitRel.setBenefit(benefit);
        benefitRel.setMember(jobApplyRecord.getReceiver());
        benefitRel.setFlow(accountFlow);
        benefitRel.setDate(truncatedDate(new Date()));
        save(benefitRel);
    }

    /**
     * 刷新好处记录
     * 1、100元车旅费 上班满一个月后自动到账户余额；每年一次，当前钱从岗位费用中扣，扣除后再进行奖励结算；
     * 2、10万工作意外险，每次上班满一个月即代表生效；如离职，则不满足条件；
     * 3、免费培训，首次参加工作，悬赏岗位工作满一个月/按月招聘工作满4个月后 即可获得“职业规划”的免费培训资格，“职业规划”没有使用时间限制；
     * 4、工资保障  自员工工作满一个月起，即生效，永久生效，店家因经营问题，不发放员工工资，员工可在此申请维权；
     * 5、快速贷款  自员工工作满一个月起，即生效，永久生效，提交所需贷款金额，即可；
     */
    public void refreshBenefitRel(BenefitRel benefitRel, AccountFlow accountFlow, JobApplyRecord jobApplyRecord) {
        Benefit benefit = benefitRel.getBenefit();

        RecruitmentType recType = accountFlow.getJob().getRecType();
        Date truncatedDate = truncatedDate(jobApplyRecord.getStartDate());//工作开始时间

        Date effectiveDate = (recType == MONTHLY && benefit.getType() == FREE_TRAING) ?
                addMonths(truncatedDate, 4) : addMonths(truncatedDate, 1);
        benefitRel.setDate(truncatedDate);
        benefitRel.setValid(recType == ONCE && benefit.getType() == FREE_TRAING ? NO : YES);
        benefitRel.setFlow(accountFlow);
        benefitRel.setEffectiveDate(effectiveDate);
        updateBenefitRelInvalidDate(benefitRel, truncatedDate);
        benefitRel.setJob(jobApplyRecord.getJob());
        ifTrueThen(recType == ONCE && benefit.getType() == FREE_TRAING, () -> processOnceFreeTrain(benefitRel, jobApplyRecord));//一次悬赏的培训
        ifTrueThen(recType == MONTHLY && benefit.getType() == FREE_TRAING, () -> processMonthFreeTrain(benefitRel, jobApplyRecord));//按月的培训
        save(benefitRel);
    }

    /**
     * 更新我的好处结束时间
     */
    private void updateBenefitRelInvalidDate(BenefitRel benefitRel, Date truncatedDate) {
        if (benefitRel.getType() == CAR_FEE) benefitRel.setInvalidDate(addYears(truncatedDate, 1));
        if (benefitRel.getType() == BenefitType.WORK_INSURANCE || benefitRel.getType() == SALARY_PROTECTION || benefitRel.getType() == BenefitType.QUICK_LOAN)
            benefitRel.setInvalidDate(addYears(truncatedDate, 50));
    }

    /**
     * 悬赏的培训
     */
    private void processOnceFreeTrain(BenefitRel benefitRel, JobApplyRecord jobApplyRecord) {
        List<BenefitItem> list = newArrayList();
        Integer times = parseTimes(benefitRel.getFlow());
        Date startDate = truncatedDate(jobApplyRecord.getStartDate());
        BenefitItem item = genBenefitItemModel(benefitRel, startDate, FREE_TRAIN_EMPLOYMENT_PLANNING);
        list.add(item);
        ifNotNullThen(benefitRel.getBenefitItem(), x -> list.addAll(x));
        for (int i = 0; i < times; i++) {
            BenefitItem e = genBenefitItemModel(benefitRel, startDate, FREE_TRAIN_NORMAL);
            list.add(e);
        }
        benefitRel.setBenefitItem(list);
    }

    /**
     * 按月的培训
     */
    private void processMonthFreeTrain(BenefitRel benefitRel, JobApplyRecord jobApplyRecord) {
        List<BenefitItem> list = newArrayList();
        Date truncatedDate = truncatedDate(jobApplyRecord.getStartDate());//工作开始时间
        benefitRel.setInvalidDate(addYears(truncatedDate, 1));//失效时间
        BenefitItem e = genBenefitItemModel(benefitRel, truncatedDate, FREE_TRAIN_EMPLOYMENT_PLANNING);
        list.add(e);
        ifNotNullThen(benefitRel.getBenefitItem(), x -> list.addAll(x));
        e.setEffectiveDate(addMonths(e.getEffectiveDate(), 3));//补上少的3个月
        benefitRel.setBenefitItem(list);
    }

    /**
     * @param benefitRel
     * @param startDate  开始时间
     * @param itemType
     * @return
     */
    public BenefitItem genBenefitItemModel(BenefitRel benefitRel, Date startDate, BenefitItemType itemType) {
        BenefitItem benefitItem = new BenefitItem();
        benefitItem.setType(itemType);
        if (itemType == FREE_TRAIN_EMPLOYMENT_PLANNING)
            benefitItem.setInvalidDate(addYears(startDate, 100));
        else benefitItem.setInvalidDate(addYears(startDate, 1));
        benefitItem.setBenefit(benefitRel.getBenefit());
        benefitItem.setEffectiveDate(addMonths(startDate, 1));
        benefitItem.setState(NORMAL);
        benefitItem.setBenefitRel(benefitRel);
        benefitItem.setValid(YES);
        return benefitItemRepository.save(benefitItem);
    }


    /*
    * 免费培训的次数
     */
    private Integer parseTimes(AccountFlow flow) {
        Integer amount = flow.getAmount().intValue();
        if (amount >= 500 && amount < 1000)
            return 1;
        else if (amount >= 1000 && amount < 1500)
            return 2;
        else if (amount >= 1500)
            return 3;
        return 0;
    }

    public Page<BenefitRel> findCarFeePage(BackPageVo pageVo, String rewardTime, Long userId) {
        Specification<BenefitRel> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member").get("id"), userId));
            ifNotBlankThen(rewardTime, e -> {
                String rewardTimeStart = e + "-01-01 00:00:00";
                String rewardTimeEnd = e + "-12-31 23:59:59";
                predicates.add(rb.greaterThan(rt.get("date"), rewardTimeStart));
                predicates.add(rb.lessThan(rt.get("date"), rewardTimeEnd));
            });
            predicates.add(rb.equal(rt.get("valid"), YES));
            predicates.add(rb.equal(rt.get("type"), CAR_FEE));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return benefitRelRepository.findAll(spec, pageVo.pageRequest());
    }

    public Page<BenefitItem> findFreeTrainPage(BackPageVo pageVo, BenefitItemState useStatus, Long userId) {
        Date now = new Date();
        BenefitRel benefitRel = benefitRelRepository.findByMemberIdAndType(userId, FREE_TRAING);
        Specification<BenefitItem> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("benefitRel").get("id"), benefitRel.getId()));
            ifNotNullThen(useStatus, t -> predicates.add(rb.equal(rt.get("state"), useStatus)));
            predicates.add(rb.lessThan(rt.get("effectiveDate"), now));
            predicates.add(rb.greaterThan(rt.get("invalidDate"), now));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        return benefitItemRepository.findAll(spec, pageVo.pageRequest());
    }

    public Page<BenefitRel> findSalaryProtectionPage(BackPageVo pageVo, Long userId) {
        Specification<BenefitRel> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member").get("id"), userId));
            predicates.add(rb.equal(rt.get("type"), SALARY_PROTECTION));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return benefitRelRepository.findAll(spec, pageVo.pageRequest());
    }

    public BenefitItem freeTrainDetail(Long id) {
        BenefitItem one = benefitItemRepository.findOne(id);
        ifNullThrow(one, TIP_NO_BENEFITREL);
        return one;
    }

    public BenefitItem updateFreeTrain(Long id, BenefitItemState useStatus, String useTo) {
        BenefitItem benefitItem = freeTrainDetail(id);
        benefitItem.setState(useStatus);
        benefitItem.setRemark(useTo);
        return benefitItemRepository.save(benefitItem);
    }
}
