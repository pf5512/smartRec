package com.thousandsunny.service.service;

import com.thousandsunny.common.DateUtil;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.AutomaticRenewalsRepository;
import com.thousandsunny.service.repository.MemberMsgRepository;
import com.thousandsunny.service.repository.RenewalsRecordRepository;
import com.thousandsunny.thirdparty.breach.BreachConfig;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.dayGap;
import static com.thousandsunny.common.DateUtil.subtractDay;
import static com.thousandsunny.common.DateUtil.subtractMonth;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.JOB_CHARGEBACK_REMIND;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.JOB_DEFAULT_REMIND;
import static com.thousandsunny.service.ModuleKey.KeyPercentage;
import static com.thousandsunny.service.ModuleKey.KeyPercentage.*;
import static com.thousandsunny.service.ModuleKey.RecState.ALREADY_RESIGN;
import static com.thousandsunny.service.ModuleKey.RecState.WORKING;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RenewType.FAILED;
import static com.thousandsunny.service.ModuleKey.RenewType.SUCCESS;
import static com.thousandsunny.service.ModuleKey.RenewalsDealType.*;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_INCOME;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.*;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.PLATFORM_INCOME;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.sql.Date.valueOf;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * 如果这些代码有用，那它们是guitarist在30/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class RenewalsRecordService extends BaseService<RenewalsRecord> {
    @Autowired
    private RenewalsRecordRepository renewalsRecordRepository;
    @Autowired
    private AutomaticRenewalsService automaticRenewalsService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private MemberRecRelService recRelService;
    @Autowired
    private MemberRegRelService regRelService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private EntrepreneursService entrepreneursService;
    @Autowired
    private MemberMsgRepository memberMsgRepository;
    @Autowired
    private AutomaticRenewalsRepository automaticRenewalsRepository;
    @Autowired
    private BreachConfig breachConfig;
    private Logger logger = getLogger(getClass());

    public RenewalsRecord findByRenewalsJobIdAndRenewalsWorkerId(Long jobId, Long workerId) {
        return renewalsRecordRepository.findTop1ByRenewalsJobIdAndRenewalsWorkerIdOrderByDateDesc(jobId, workerId);
    }

    public List<RenewalsRecord> findByDateLessThanAndAssigned(Date oneMonthBefore, Boolean assigned) {
        return renewalsRecordRepository.findByDateLessThanAndAssigned(oneMonthBefore, assigned);
    }

    public void chargeMonthlyFee() {
        java.sql.Date now = valueOf(LocalDate.now());
        List<AutomaticRenewals> automaticRenewals = automaticRenewalsService.findAutoRenewalRecord(now);
        for (AutomaticRenewals r : automaticRenewals) {
            JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobIdAndRecState(r.getWorker().getToken(), r.getJob().getId(), WORKING);
            if (jobApplyRecord == null) continue;
            Account memberAccount = r.getAccount();
            BigDecimal payAmount = r.getFee().add(r.getBreach());//需要付金额
            BigDecimal balance = memberAccount.getBalance();//账户余额/renew
            if (balance.compareTo(payAmount) != -1) {//账户余额够支付这一笔续费
                accountService.memberAccountPayMoney(memberAccount, payAmount);//变动账户信息

                AccountFlow accountFlow = jobApplyRecordService.saveAccountFlow(jobApplyRecord, memberAccount, payAmount, JOB_RENEW);//产生流水
                saveRenewalRecord(r, accountFlow);//保存续费记录
                jobApplyRecordService.refreshAutoRenewalRecord(r);//刷新自动续费记录
                sendChargeMessage(jobApplyRecord, r, "您的悬赏招聘费用已从账户余额中自动扣除");
            } else {// 不够支付_违约的逻辑(余额不足以支付，保存失败的续费记录)
                saveFalseRenewalRecord(r);
                sendChargeMessage(jobApplyRecord, r, "您的悬赏招聘需要预付费用，当前账户余额不足支付，请尽快付款！");
            }
        }
    }

    private void saveFalseRenewalRecord(AutomaticRenewals r) {
        RenewalsRecord renewalsRecord = renewalsRecordRepository.findByRenewalsIdAndTimesAndRenewType(r.getId(), r.getTimes(), FAILED);
        if (!isNotNull(renewalsRecord)) {
            renewalsRecord = new RenewalsRecord();
            renewalsRecord.setRenewType(FAILED);
            renewalsRecord.setAssigned(FALSE);
            renewalsRecord.setRenewals(r);
            renewalsRecord.setJob(r.getJob());
            renewalsRecord.setTimes(r.getTimes());
            renewalsRecord.setStartDate(r.getStartDate());
            renewalsRecord.setFinalDate(r.getFinalDate());
        }
        renewalsRecord.setDate(new Date());
        save(renewalsRecord);
    }

    /**
     * 给店家发送岗位续费扣款信息（成功，余额不足）
     *
     * @Author xiao xue wei
     * @Date 2017/1/10
     */
    private void sendChargeMessage(JobApplyRecord jobApplyRecord, AutomaticRenewals r, String content) {
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setJob(r.getJob());
        memberMsg.setType(JOB_CHARGEBACK_REMIND);
        memberMsg.setJobApplyRecord(jobApplyRecord);
        memberMsg.setReceiver(r.getAccount().getMember());
        memberMsg.setContent(content);
        List<MemberMsg> msgList = memberMsgRepository.findByReceiverAndIsDeleteAndTypeOrderByDateDesc(
                r.getAccount().getMember(), NO, JOB_CHARGEBACK_REMIND);
        if (!msgList.isEmpty()) {
            msgList.forEach(msg -> msg.setIsNew(NO));
            memberMsgRepository.save(msgList);
        }
        memberMsgRepository.save(memberMsg);
    }

    /**
     * 保存续费记录
     */
    private void saveRenewalRecord(AutomaticRenewals automaticRenewals, AccountFlow accountFlow) {
        RenewalsRecord renewalsRecord = renewalsRecordRepository.findByRenewalsIdAndTimesAndRenewType(automaticRenewals.getId(), automaticRenewals.getTimes(), FAILED);
        if (!isNotNull(renewalsRecord)) {
            renewalsRecord = new RenewalsRecord();
            renewalsRecord.setAssigned(FALSE);
            renewalsRecord.setRenewals(automaticRenewals);
            renewalsRecord.setJob(automaticRenewals.getJob());
            renewalsRecord.setTimes(automaticRenewals.getTimes());
            renewalsRecord.setStartDate(automaticRenewals.getStartDate());
            renewalsRecord.setFinalDate(automaticRenewals.getFinalDate());
        }
        renewalsRecord.setDate(new Date());
        renewalsRecord.setFee(accountFlow.getAmount());//可以被分配的资金
        renewalsRecord.setAccountFlow(accountFlow);
        renewalsRecord.setRenewType(SUCCESS);
        save(renewalsRecord);
    }

    /**
     * 平台,合伙人,(车马费:上班的人)
     * 会员续的费,进行分钱
     * 推荐人        推荐关系    注册关系
     * 朋友（一级)   30%         10%
     * <p>
     * 熟人（二级)   10%         5%
     * <p>
     * 人脉（三级)   5%          5%
     */
    public void spoilsDaily() {
        Date oneMonthBefore = subtractMonth(new Date(), 1);
        List<RenewalsRecord> renewalsRecords = findByDateLessThanAndAssigned(oneMonthBefore, FALSE);
        for (RenewalsRecord record : renewalsRecords) {
            BigDecimal remain = record.getFee();//总共分配时候的金钱
            if (isNotNull(remain)) {

                if (record.getJob().getRecType() == MONTHLY) {
                    AutomaticRenewals renewals = record.getRenewals();
                    if (isNotNull(renewals)) {
                        JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(renewals.getWorker().getId(), record.getJob().getId());
                        if (isNotNull(jobApplyRecord) && jobApplyRecord.getRecState() != WORKING && jobApplyRecord.getRecState() != ALREADY_RESIGN) {
                            continue;
                        }
                    }
                }

                BigDecimal recPaid = processRecRel(record);//推荐管理
                BigDecimal regPaid = processRegRel(record);//注册关系
                BigDecimal partnerPaid = processPartner(record);//处理合伙人
                accountFlowService.processPlatform(record, remain.subtract(recPaid).subtract(regPaid).subtract(partnerPaid), PLATFORM_PROFIT_JOB, PLATFORM_INCOME,
                        null, null, null, null, null);//处理平台收入逻辑
            }
            record.setAssigned(TRUE);
            save(record);
        }
    }

    /**
     * 转钱
     *
     * @param getMoneyMemberId 进账用户
     */
    private BigDecimal assignAndReturnActualPaied(BigDecimal payedMoney, Long getMoneyMemberId, RenewalsRecord renewalsRecord, SourceType sourceType, KeyPercentage keyPercentage) {
        if (isNotNull(getMoneyMemberId)) {
            Member member = memberService.findOne(getMoneyMemberId);
            String userToken = member.getToken();
            Account memberAccount = accountService.findByMemberToken(userToken);
            accountService.freezeZuesAccountRefundMoney(memberAccount, payedMoney, sourceType.getRemark());//转钱
            BigDecimal reward = null;
            if (isNotNull(renewalsRecord.getJob()))
                reward = renewalsRecord.getJob().getReward();
            accountFlowService.saveAccountFlow(renewalsRecord.getAccountFlow(), payedMoney, sourceType, memberAccount, reward, keyPercentage);//产生个人进账流水
            Account platform = accountService.findZuesAccount();
            accountFlowService.savePlatformFlow(null, renewalsRecord.getJob(), platform, payedMoney, sourceType, BILL_INCOME, null, null, reward);//产生平台出账流水
            Entrepreneurs entrepreneurs = entrepreneursService.findEntrepreneurs(userToken);
            //创业者收益增加
            ifTrueThen(entrepreneurs != null && (sourceType == ENTREPRENEUR_RECOMMEND_AWARD || sourceType == ENTREPRENEUR_REGISTER_AWARD), () -> {
                entrepreneurs.setEntrepreneurRewardIncome(entrepreneurs.getEntrepreneurRewardIncome().add(payedMoney));
                entrepreneurs.setIncome(entrepreneurs.getIncome().add(payedMoney));
                entrepreneursService.save(entrepreneurs);
            });
            return payedMoney;
        }
        return new BigDecimal(0);
    }

    /**
     * 推荐关系
     * 1.得到推荐关系
     */
    private BigDecimal processRecRel(RenewalsRecord record) {
        BigDecimal originalAssignable = record.getFee();//可分配的金额

        Member employee = record.getAccountFlow().getJobApplyRecord().getReceiver();
        Map<String, Long> rec_p1p2p3 = recRelService.recCascade(employee.getId());

        BigDecimal p1NeedToPay = wrapRecAction(record, originalAssignable, rec_p1p2p3, "id1", CONSTANT_REC_FRIEND);

        BigDecimal p2NeedToPay = wrapRecAction(record, originalAssignable, rec_p1p2p3, "id2", CONSTANT_REC_ACQUAINTANCE);

        BigDecimal p3NeedToPay = wrapRecAction(record, originalAssignable, rec_p1p2p3, "id3", CONSTANT_REC_CONTACTS);

        return p1NeedToPay.add(p2NeedToPay).add(p3NeedToPay);
    }

    /**
     * 推荐收益
     */
    private BigDecimal wrapRecAction(RenewalsRecord record, BigDecimal originalAssignable, Map<String, Long> rec_p1p2p3, String id, KeyPercentage keyPercentage) {
        BigDecimal needToPay = new BigDecimal(0);
        Long _id = rec_p1p2p3.get(id);
        if (entrepreneursService.isEp(_id)) {
            needToPay = keyPercentage.needToPay(originalAssignable);
            assignAndReturnActualPaied(needToPay, _id, record, ENTREPRENEUR_RECOMMEND_AWARD, keyPercentage);
        }
        return needToPay;
    }

    /**
     * 注册关系
     * 1.得到注册关系
     */
    private BigDecimal processRegRel(RenewalsRecord record) {
        BigDecimal originalAssignable = record.getFee();//可分配的金额

        Member employee = record.getAccountFlow().getJobApplyRecord().getReceiver();
        Map<String, Long> reg_p1p2p3 = regRelService.regCascade(employee.getId());
        BigDecimal p1NeedToPay = wrapRegAction(record, originalAssignable, reg_p1p2p3, "id1", CONSTANT_REG_FRIEND);
        BigDecimal p2NeedToPay = wrapRegAction(record, originalAssignable, reg_p1p2p3, "id2", CONSTANT_REG_ACQUAINTANCE);
        BigDecimal p3NeedToPay = wrapRegAction(record, originalAssignable, reg_p1p2p3, "id3", CONSTANT_REG_CONTACTS);

        return p1NeedToPay.add(p2NeedToPay).add(p3NeedToPay);
    }

    /**
     * 注册收益
     */
    private BigDecimal wrapRegAction(RenewalsRecord record, BigDecimal originalAssignable, Map<String, Long> reg_p1p2p3, String id, KeyPercentage keyPercentage) {
        BigDecimal needToPay = new BigDecimal(0);
        Long getMoneyMemberId = reg_p1p2p3.get(id);
        if (entrepreneursService.isEp(getMoneyMemberId)) {
            needToPay = keyPercentage.needToPay(originalAssignable);
            assignAndReturnActualPaied(needToPay, getMoneyMemberId, record, ENTREPRENEUR_REGISTER_AWARD, keyPercentage);
        }
        return needToPay;
    }

    /**
     * 处理合伙人
     */
    private BigDecimal processPartner(RenewalsRecord record) {
        SourceType sourceType = record.getAccountFlow().getJob().getRecType() == MONTHLY ? PARTNER_MONTHLY_AWARD : PARTNER_ONCE_AWARD;

        BigDecimal originalAssignable = record.getFee();//可分配的金额
        BigDecimal needPay = new BigDecimal(0);
        Region area = record.getAccountFlow().getJob().getShop().getArea();//工作的区域
        Partner partner = partnerService.findPartner(area);
        if (isNotNull(partner)) {
            needPay = CONSTANT_PARTNER.needToPay(originalAssignable);
            Long getMoneyMemberId = partner.getMember().getId();
            if (partnerService.isPartner(getMoneyMemberId)) {
                assignAndReturnActualPaied(needPay, getMoneyMemberId, record, sourceType, CONSTANT_PARTNER);
            }
            partner.setIncome(partner.getIncome().add(needPay));
            partnerService.save(partner);
        }
        return needPay;
    }

    /**
     * 岗位招聘付款违约提示
     *
     * @Author xiao xue wei
     * @Date 2017/1/10
     */
    public void sendDefaultMessage() {
        java.sql.Date now = valueOf(LocalDate.now());
        List<AutomaticRenewals> list = automaticRenewalsRepository.findByIsDeleteAndFinalDateLessThan(NO, now);
        Date nowTime = new Date(now.getTime());
        list.forEach(automaticRenewals -> {
            RenewalsRecord renewalsRecord = renewalsRecordRepository.
                    findByRenewalsIdAndTimesAndRenewType(automaticRenewals.getId(), automaticRenewals.getTimes(), FAILED);
            ifNotNullThen(renewalsRecord, x -> {
                //计算违约金保存
                Integer days = DateUtil.dayGap(new Date(automaticRenewals.getFinalDate().getTime()), nowTime);//违约天数
                BigDecimal breach = ((automaticRenewals.getFee().multiply(new BigDecimal(breachConfig.getScale())).multiply(new BigDecimal(days)))
                        .setScale(0, BigDecimal.ROUND_UP)).add(new BigDecimal(breachConfig.getMinBreach()));
                x.setBreach(breach);
                x.setDealType(DO_NOT_DEAL);
                automaticRenewals.setBreach(breach);
                renewalsRecordRepository.save(x);
                automaticRenewalsService.save(automaticRenewals);
                //发送违约提示
                sendDefaultMsg(automaticRenewals, "您的悬赏招聘没有按时付费，平台开始计算违约金啦！");
            });
        });
    }

    private void sendDefaultMsg(AutomaticRenewals renewalsRecord, String content) {
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(renewalsRecord.getWorker().getId(), renewalsRecord.getJob().getId());
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setJob(renewalsRecord.getJob());
        memberMsg.setType(JOB_DEFAULT_REMIND);
        memberMsg.setJobApplyRecord(jobApplyRecord);
        memberMsg.setReceiver(renewalsRecord.getAccount().getMember());
        memberMsg.setContent(content);
        List<MemberMsg> msgList = memberMsgRepository.findByReceiverAndIsDeleteAndTypeOrderByDateDesc(
                renewalsRecord.getAccount().getMember(), NO, JOB_DEFAULT_REMIND);

        if (!msgList.isEmpty()) {
            msgList.forEach(msg -> msg.setIsNew(NO));
            memberMsgRepository.save(msgList);
        }
        memberMsgRepository.save(memberMsg);
    }

    /**
     * 岗位招聘余额付款余额不足的七天内每天提示一次
     *
     * @Author xiao xue wei
     * @Date 2017/1/10
     */
    public void sendChargeMsg() {
        java.sql.Date now = valueOf(LocalDate.now());
        Specification<AutomaticRenewals> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            predicates.add(rb.lessThanOrEqualTo(rt.get("startDate"), now));
            predicates.add(rb.greaterThan(rt.get("finalDate"), now));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<AutomaticRenewals> list = automaticRenewalsRepository.findAll(spec);
        ifNotEmptyThen(list, list_ -> list_.forEach(automaticRenewals -> {
            RenewalsRecord renewalsRecord = renewalsRecordRepository.
                    findByRenewalsIdAndTimesAndRenewType(automaticRenewals.getId(), automaticRenewals.getTimes(), FAILED);
            JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(automaticRenewals.getWorker().getId(), automaticRenewals.getJob().getId());
            ifNotNullThen(renewalsRecord, x -> sendChargeMessage(jobApplyRecord, automaticRenewals, "您的悬赏招聘需要预付费用，当前账户余额不足支付，请尽快付款！"));
        }));

    }

    /**
     * 后台管理续费违约列表
     *
     * @Author xiao xue wei
     * @Date 2017/1/11
     */
    public Page<RenewalsRecord> findContractList(Pageable pageable, String text, String tableType) {
        Specification<RenewalsRecord> spec = (rt, rq, rb) -> {
            List<ModuleKey.RenewalsDealType> dealTypes = newArrayList(DEAL_BY_RENEWAL, DEAL_BY_RETURN, DEAL_BY_PLATFORM);
            List<Predicate> predicates = newArrayList();
            predicates.add(rt.get("renewals").isNotNull());
            predicates.add(rb.lessThan(rt.get("finalDate"), valueOf(LocalDate.now())));
            predicates.add(rt.get("breach").isNotNull());
            if ("has_deal".equals(tableType)) predicates.add(rt.get("dealType").in(dealTypes));
            else if ("do_not_deal".equals(tableType)) predicates.add(rb.equal(rt.get("dealType"), DO_NOT_DEAL));
            ifNotBlankThen(text, t -> {
                String textStr = "%" + t + "%";
                predicates.add(rb.or(rb.like(rt.get("job").get("shop").get("name"), textStr),
                        rb.like(rt.get("renewals").get("worker").get("realName"), textStr)));
            });
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return renewalsRecordRepository.findAll(spec, pageable);
    }

    /**
     * 平台处理违约---修改续费记录
     *
     * @Author xiao xue wei
     * @Date 2017/1/12
     */
    public String editRenewalsRecord(Long id, BigDecimal amount, String remark) {
        RenewalsRecord renewalsRecord = renewalsRecordRepository.findOne(id);
        renewalsRecord.setDealType(DEAL_BY_PLATFORM);
        ifNotBlankThen(remark, x -> renewalsRecord.setRemark(x));
        ifNotNullThen(amount, x -> renewalsRecord.setBreach(x));
        save(renewalsRecord);
        return "success";
    }

    /**
     * 平台处理违约---删除自动续费
     *
     * @Author xiao xue wei
     * @Date 2017/2/24
     */
    public void deleteAutoRenewals(AutomaticRenewals renewals) {
        if (isNotNull(renewals)) {
            renewals.setIsDelete(YES);
            automaticRenewalsService.save(renewals);
        }
    }
}
