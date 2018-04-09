package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageRequest;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.ModuleKey.ApplyEnum;
import com.thousandsunny.service.ModuleKey.EntrepreneursType;
import com.thousandsunny.service.ModuleKey.RecruitmentType;
import com.thousandsunny.service.ModuleKey.WithdrawType;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.EntrepreneursApplyRepository;
import com.thousandsunny.service.repository.LinePaymentBankRepository;
import com.thousandsunny.service.repository.PartnerApplyRepository;
import com.thousandsunny.thirdparty.ModuleKey.*;
import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.apache.tomcat.jdbc.pool.interceptor.SlowQueryReport;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.AccountEnum.SCHOOL;
import static com.thousandsunny.service.ModuleKey.RecruitmentState;
import static com.thousandsunny.service.ModuleKey.RecruitmentState.*;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_IN;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.*;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_OFFLINE;
import static com.thousandsunny.thirdparty.ModuleKey.*;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.*;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.*;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateUtils.addMonths;
import static org.springframework.util.CollectionUtils.arrayToList;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;

/**
 * 如果这些代码有用，那它们是guitarist在01/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class AccountFlowService extends BaseService<AccountFlow> {
    @Autowired
    private AccountFlowRepository accountFlowRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private JobService jobService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private EntrepreneursService entrepreneursService;
    @Autowired
    private EntrepreneursApplyRepository entrepreneursApplyRepository;
    @Autowired
    private PartnerApplyRepository partnerApplyRepository;
    @Autowired
    private CourseApplyService courseApplyService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private LinePaymentBankRepository linePaymentBankRepository;


    private List<SourceType> typeList = newArrayList(JOB_NEW, JOB_ADD, JOB_CUT, JOB_RENEW, JOB_RESIGN, JOB_REFUND);
    private List<SourceType> typeListApproval = newArrayList(JOB_NEW, JOB_ADD, JOB_RENEW);
    private List<FlowState> states = newArrayList(SUCCESS, FAILED);
    private static final ArrayList<SourceType> ENTREPRENEURS_REWARD_TYPE = newArrayList(ENTREPRENEUR_AWARD, ENTREPRENEUR_RECOMMEND_AWARD, ENTREPRENEUR_REGISTER_AWARD);
    private static final List<RecruitmentType> ENTREPRENEURS_REWARD_RECRUITTYPE_TYPE = arrayToList(RecruitmentType.values());
    private static final String[] stateList = {RecruitmentState.PAY_OFFLINE.name(), NORMAL.name(), PAUSE.name(), STOP.name(), FROZEN.name(), DELETE.name()};


    public Page<AccountFlow> findAccountFlowList(BackPageVo backPageVo, String text,
                                                 FlowState tableType, Date startTime, Date endTime,
                                                 EntrepreneursType estpType, PayType payWay,
                                                 String str, FlowState payStatus, SourceType opType, RecruitmentType recType) {
        Account platform = accountService.findZuesAccount();
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.notEqual(rt.get("account").get("id"), platform.getId()));
//            predicates.add(rb.equal(rt.get("type"), PAY_OUT));
            if (str.equals("entrepreneurs")) {
                ifNotBlankThen(text, t -> predicates.add(rb.or(
                        rb.like(rt.get("entrepreneursApply").get("member").get("realName"), "%" + t + "%"),
                        rb.like(rt.get("entrepreneursApply").get("member").get("hpAccount"), "%" + t + "%")
                )));
                predicates.add(rb.equal(rt.get("source"), ENTREPRENEUR_APPLY));
                predicates.add(rb.equal(rt.get("state"), tableType));
            } else if (str.equals("partner")) {
                ifNotBlankThen(text, t -> predicates.add(rb.or(
                        rb.like(rt.get("partnerApply").get("member").get("realName"), "%" + t + "%"),
                        rb.like(rt.get("partnerApply").get("member").get("hpAccount"), "%" + t + "%")
                )));
                predicates.add(rb.equal(rt.get("state"), tableType));
                predicates.add(rb.equal(rt.get("source"), PARTNER_APPLY));
            } else if (str.equals("job")) {
                ifNotBlankThen(text, t -> predicates.add(rb.or(
                        rb.like(rt.get("job").get("shop").get("name"), "%" + t + "%"),
                        rb.like(rt.get("job").get("name"), "%" + t + "%")
                )));
                ifNotNullThen(recType, t -> predicates.add(rb.equal(rt.get("job").get("recType"), recType)));
                if (tableType == APPROVAL) {
                    predicates.add(rb.equal(rt.get("state"), APPROVAL));
                    predicates.add(rt.get("source").in(typeListApproval));
                } else {
                    predicates.add(rt.get("state").in(states));
                    predicates.add(rt.get("source").in(typeList));
                }
                ifNotNullThen(payStatus, t -> predicates.add(rb.equal(rt.get("state"), t)));
                ifNotNullThen(opType, t -> predicates.add(rb.equal(rt.get("source"), t)));
            }
            ifNotNullThen(startTime, t -> predicates.add(rb.greaterThan(rt.get("updateDate"), t)));
            ifNotNullThen(endTime, t -> predicates.add(rb.lessThan(rt.get("updateDate"), t)));
            ifNotNullThen(estpType, t -> predicates.add(rb.equal(rt.get("entrepreneursApply").get("type"), t)));
            ifNotNullThen(payWay, t -> predicates.add(rb.equal(rt.get("payType"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("updateDate"), false)).getRestriction();
        };
        return accountFlowRepository.findAll(spec, backPageVo.pageRequest());
    }

    public Page<AccountFlow> findCourseApplyAccountFlowList(Pageable pageable, String text, FlowState tableType, Date startTime, Date endTime, Member member) {
        Account platform = accountService.findZuesAccount();
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if (member.getRole() == SCHOOL)
                predicates.add(rb.equal(rt.get("account").get("member").get("id"), member.getId()));
            predicates.add(rb.notEqual(rt.get("account").get("id"), platform.getId()));
            predicates.add(rb.equal(rt.get("type"), PAY_OUT));
            ifNotBlankThen(text, t -> predicates.add(rb.or(
                    rb.like(rt.get("courseApply").get("member").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("courseApply").get("member").get("mobile"), "%" + t + "%"),
                    rb.like(rt.get("courseApply").get("course").get("name"), "%" + t + "%")
            )));
            if (tableType == APPROVAL) {
                predicates.add(rb.equal(rt.get("state"), APPROVAL));
            } else predicates.add(rt.get("state").in(states));
            predicates.add(rb.equal(rt.get("source"), COURSE_APPLY));
            ifNotNullThen(startTime, t -> predicates.add(rb.greaterThan(rt.get("updateDate"), t)));
            ifNotNullThen(endTime, t -> predicates.add(rb.lessThan(rt.get("updateDate"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("updateDate"), false)).getRestriction();
        };
        return accountFlowRepository.findAll(spec, pageable);
    }

    public Page<AccountFlow> findWihtdrawList(BackPageVo backPageVo, String text, FlowState tableType, Date startTime,
                                              Date endTime, FlowState cashStatus, WithdrawType cashAccount) {
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if (tableType == APPROVAL) {
                predicates.add(rb.equal(rt.get("state"), APPROVAL));
            } else predicates.add(rt.get("state").in(states));
            predicates.add(rb.equal(rt.get("recordType"), BILL_WITHDRAW));
            ifNotNullThen(startTime, t -> predicates.add(rb.greaterThan(rt.get("createDate"), t)));
            ifNotNullThen(endTime, t -> predicates.add(rb.lessThan(rt.get("createDate"), t)));
            ifNotBlankThen(text, t -> predicates.add(rb.or(rb.like(rt.get("withdrawAccount").get("member").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("withdrawAccount").get("account"), "%" + t + "%"))));
            ifNotNullThen(cashStatus, t -> predicates.add(rb.equal(rt.get("state"), cashStatus)));
            ifNotNullThen(cashAccount, t -> predicates.add(rb.equal(rt.get("withdrawAccount").get("type"), t)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        return accountFlowRepository.findAll(spec, backPageVo.pageRequest());
    }

    public String editWithdraw(Long id, FlowState cashStatus, String bankFlow, Long payBank, Date payTime, String reson) {
        //判断流水存在和流水类型是否为提现
        AccountFlow accountFlow = accountFlowRepository.findOne(id);
        ifNullThrow(accountFlow, TIP_NO_ACCOUNT_FLOW);
        ifFalseThrow(accountFlow.getRecordType().equals(BILL_WITHDRAW), TIP_ACCOUNT_FLOW_TYPE_FALSE);
        //判断提现状态
        if (cashStatus.equals(FAILED)) {
            accountFlow.setState(cashStatus);
            ifNotBlankThen(reson, accountFlow::setReason);
            accountFlowRepository.save(accountFlow);

            // 审核不通过，将冻结金额返回到余额和总金额中
            Account account = accountFlow.getAccount();
            BigDecimal freezingAmount = account.getFreezingAmount();
            account.setFreezingAmount(freezingAmount.subtract(accountFlow.getAmount()));
            BigDecimal balance = account.getBalance();
            account.setBalance(balance.add(accountFlow.getAmount()));
            account.setTotal(account.getTotal().add(accountFlow.getAmount()));
            accountRepository.save(account);
        } else {
            ifNotNullThen(cashStatus, accountFlow::setState);
            JSONObject jsonObject = new JSONObject();
            ifNotBlankThen(bankFlow, x -> jsonObject.put("bankFlow", x));
            ifNotNullThen(payBank, x -> {
                LinePaymentBank linePaymentBank = linePaymentBankRepository.findByIdAndIsDelete(x, NO);
                ifNullThrow(linePaymentBank, TIP_NO_BANK);
                jsonObject.put("payBank", linePaymentBank.getBankName());
            });
            ifNotNullThen(payTime, x -> jsonObject.put("payTime", x));
            ifNotBlankThen(reson, x -> jsonObject.put("remark", x));
            accountFlow.setRemarks(jsonObject.toJSONString());
            accountFlow.setUpdateDate(new Date());
            accountFlowRepository.save(accountFlow);

            // 扣除账户冻结金额
            Account account = accountFlow.getAccount();
            BigDecimal freezingAmount = account.getFreezingAmount();
            account.setFreezingAmount(freezingAmount.subtract(accountFlow.getAmount()));
            accountRepository.save(account);
        }
        return "success";
    }

    /**
     * 处理平台收入逻辑
     * <p>
     * 保存业务流水
     * fixme 收益流水还有"创业收益"和"合伙收益",这里只是"岗位收益"
     */
    AccountFlow processPlatform(RenewalsRecord record, BigDecimal profit, SourceType sourceType, RecordType recordType,
                                PartnerApply partnerApply, EntrepreneursApply entrepreneursApply, Job job, JobApplyRecord jobApplyRecord, PayType payType) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(accountService.findZuesAccount());
        accountFlow.setAmount(profit);
        accountFlow.setPayType(payType);
        accountFlow.setType(PAY_IN);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRecordType(recordType);
        accountFlow.setSource(sourceType);
        accountFlow.setState(FlowState.SUCCESS);
        ifNotNullThen(entrepreneursApply, accountFlow::setEntrepreneursApply);
        setAccountFlowRemark(accountFlow, null);
        ifNotNullThen(job, accountFlow::setJob);
        ifNotNullThen(jobApplyRecord, accountFlow::setJobApplyRecord);
        ifNotNullThen(record, x -> {
            ifNotNullThen(x.getAccountFlow().getJob(), accountFlow::setJob);
            ifNotNullThen(x.getAccountFlow().getJobApplyRecord(), accountFlow::setJobApplyRecord);
        });
        ifNotNullThen(partnerApply, accountFlow::setPartnerApply);
        return save(accountFlow);
    }

    public AccountFlow processCourseRefundAccountFlow(CourseApply courseApply, CourseRefundApply courseRefundApply,
                                                      Account account, BigDecimal money, ChargeType type) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(account);
        accountFlow.setAmount(money);
        accountFlow.setType(type);
        accountFlow.setRecordType(BILL_REFUND);
        accountFlow.setSource(COURSE_REFUND);
        if (type == PAY_IN) {
            accountFlow.setState(FlowState.APPROVAL);
        } else accountFlow.setState(FlowState.SUCCESS);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setCourseApply(courseApply);
        accountFlow.setCourseRefundApply(courseRefundApply);
        setAccountFlowRemark(accountFlow, null);
        return save(accountFlow);
    }

    private AccountFlow processJob(BigDecimal money, Job job, JobApplyRecord jobApplyRecord, SourceType source) {
        Account account = accountService.findByMemberToken(job.getShop().getOwner().getToken());
        AccountFlow accountFlow = new AccountFlow();
        ifNotNullThen(job, accountFlow::setJob);
        ifNotNullThen(jobApplyRecord, accountFlow::setJobApplyRecord);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setAmount(money);
        accountFlow.setAccount(account);
        accountFlow.setType(PAY_OUT);
        accountFlow.setRecordType(BILL_PAY_OFFLINE);
        accountFlow.setSource(source);
        accountFlow.setState(FlowState.SUCCESS);
        setAccountFlowRemark(accountFlow, null);
        accountFlow.setPayType(PAY_OFFLINE);
        return save(accountFlow);
    }

    public void setAccountFlowRemark(AccountFlow accountFlow, BigDecimal reward) {
        SourceType sourceType = accountFlow.getSource();
        if (reward != null && (sourceType == PARTNER_ONCE_AWARD || sourceType == PARTNER_MONTHLY_AWARD)) {
            accountFlow.setRemark(sourceType.getRemark().replace("?", reward.toString()));
        } else if (sourceType == ENTREPRENEUR_APPLY) {
            EntrepreneursApply entrepreneursApply = accountFlow.getEntrepreneursApply();
            accountFlow.setRemark(sourceType.getRemark().replace("?", entrepreneursApply.getType().getTitle()));
        } else {
            accountFlow.setRemark(sourceType.getRemark());
        }
    }

    public BigDecimal countJoinMoney(FlowState tableType, ModuleKey.EntrepreneursType type, String text, Date startTime, Date endTime, EntrepreneursType estpType, PayType payWay) {
        StringBuffer sb = new StringBuffer();
        sb.append("select SUM(entreprene1_.join_money) as col_0_0_  from tp_account_flow accountflo0_ join sr_entrepreneurs_apply entreprene1_ " +
                " join  core_member member2_  where accountflo0_.entrepreneurs_apply_id=entreprene1_.id and entreprene1_.member_id=member2_.id " +
                " and accountflo0_.state= '" + tableType + "'");
        sb.append(" and accountflo0_.source = 'ENTREPRENEUR_APPLY'");
        sb.append(" and accountflo0_.type = '" + PAY_OUT + "'");
        sb.append(" and entreprene1_.type = '" + type + "'");
        ifNotBlankThen(text, t -> sb.append(" and ( member2_.real_name like '%" + t + "%'  or member2_.hp_account like '%" + t + "%'  ) "));
        ifNotNullThen(startTime, t -> sb.append(" and accountflo0_.create_date>'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(endTime, t -> sb.append(" and accountflo0_.create_date<'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(estpType, t -> sb.append(" and entreprene1_.type= '" + t + "'"));
        ifNotNullThen(payWay, t -> sb.append(" and accountflo0_.pay_type='" + t + "'"));
        Query query = entityManager.createNativeQuery(sb.toString());
        BigDecimal singleResult = (BigDecimal) query.getSingleResult();
        return singleResult;
    }

    public void updateAccountFlow(Long id, FlowState state, Date date, String username, PayOfflineType payWay, String payBank, String bankNO, String bankFlow, String receiveBank, String content) {
        AccountFlow accountFlow = accountFlowRepository.findOne(id);
        ifNotNullThen(state, accountFlow::setState);
        ifNotNullThen(date, accountFlow::setUpdateDate);
        ifNotNullThen(payWay, accountFlow::setPayOfflineType);
        JSONObject jsonObject = new JSONObject();
        ifNotBlankThen(username, x -> jsonObject.put("username", x));
        ifNotBlankThen(payBank, x -> jsonObject.put("payBank", x));
        ifNotBlankThen(bankNO, x -> jsonObject.put("bankNO", x));
        ifNotBlankThen(bankFlow, x -> jsonObject.put("bankFlow", x));
        ifNotBlankThen(receiveBank, x -> jsonObject.put("receiveBank", x));
        ifNotBlankThen(content, x -> jsonObject.put("content", x));
        accountFlow.setRemarks(jsonObject.toJSONString());
        if (state == SUCCESS) {
            //岗位确认付款
            ifNotNullThen(accountFlow.getJob(), job -> {
                job.setState(NORMAL);
                job.setDate(new Date());
                if (accountFlow.getSource() == JOB_ADD) {
                    job.setEpmCount(job.getEpmCount() + job.getChangeCount());
                    job.setChangeCount(0);
                }
                jobService.save(job);
                //往系统账户中加钱，自己的账户余额不变
                Account zuesAccount = accountService.findZuesAccount();
                zuesAccount.setBalance(zuesAccount.getBalance().add(accountFlow.getAmount()));
                zuesAccount.setTotal(zuesAccount.getTotal().add(accountFlow.getAmount()));
                accountService.save(zuesAccount);
                processPlatform(null, accountFlow.getAmount(), JOB_ADD, PLATFORM_IN, null, null, job, accountFlow.getJobApplyRecord(), PAY_OFFLINE);//平台产生流水

                jobService.publishJobMoments(job.getShop().getOwner().getToken(), job, job.getShop());//发布说说
            });
            ifNotNullThen(accountFlow.getPartnerApply(), partnerService::confirmPartnerApply);//合伙人确认线下付款
            ifNotNullThen(accountFlow.getEntrepreneursApply(), entrepreneursService::confirmEntrepreneursApply);//创业者确认线下付款
            ifNotNullThen(accountFlow.getCourseApply(), courseApplyService::confirmCourseApply);//课程付款确认线下付款
        }
        accountFlowRepository.save(accountFlow);
    }

    public BigDecimal countPartentJoinMoney(FlowState tableType, String text, Date startTime, Date endTime, PayType payWay) {
        StringBuffer sb = new StringBuffer();
        sb.append("select SUM(partnerapp1_.join_money) as col_0_0_   from  tp_account_flow accountflo0_ cross  join sr_partner_apply partnerapp1_ cross " +
                " join core_member member2_  where accountflo0_.partner_apply_id=partnerapp1_.id and partnerapp1_.member_id=member2_.id " +
                " and accountflo0_.state= '" + tableType + "'");
        sb.append(" and accountflo0_.source = 'PARTNER_APPLY'");
        sb.append(" and accountflo0_.type = '" + PAY_OUT + "'");
        ifNotBlankThen(text, t -> sb.append("  and (member2_.real_name like '%" + t + "%' or member2_.hp_account like '%" + t + "%' ) "));
        ifNotNullThen(startTime, t -> sb.append(" and accountflo0_.create_date>'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(endTime, t -> sb.append(" and accountflo0_.create_date<'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(payWay, t -> sb.append(" and accountflo0_.pay_type='" + t + "'"));
        Query query = entityManager.createNativeQuery(sb.toString());
        BigDecimal singleResult = (BigDecimal) query.getSingleResult();
        return singleResult;
    }

    public BigDecimal countRecruit(String text, Date startTime, Date endTime, FlowState payStatus, PayType payWay, SourceType opType, SourceType source) {
        StringBuffer sql = new StringBuffer();
        sql.append("select  SUM(accountflo0_.amount) as col_0_0_ from tp_account_flow accountflo0_ " +
                " left join  sr_job_apply_record jobapplyre1_ on accountflo0_.job_apply_record_id=jobapplyre1_.id " +
                " left join sr_shop shop2_ on jobapplyre1_.shop_id=shop2_.id" +
                " left join sr_job job4_  on jobapplyre1_.job_id=job4_.id ");
        sql.append(" where accountflo0_.source='" + source + "'");
        sql.append(" and accountflo0_.type = '" + PAY_OUT + "'");
//        sql.append(" and ( accountflo0_.state in " + states + ") and ( accountflo0_.source in " + typeList + ") ");
        sql.append(" and ( accountflo0_.state in ('SUCCESS', 'FAILED')) and ( accountflo0_.source in ('JOB_NEW', 'JOB_ADD', 'JOB_CUT', 'JOB_RENEW', 'JOB_RESIGN', 'JOB_REFUND')) ");
        ifNotBlankThen(text, t -> sql.append(" and ( shop2_.name like '%" + t + "%'  or job4_.name like '%" + t + "%' ) "));
        ifNotNullThen(startTime, t -> sql.append(" and accountflo0_.create_date>'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(endTime, t -> sql.append(" and accountflo0_.create_date<'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(payWay, t -> sql.append(" and accountflo0_.pay_type='" + t + "'"));
        ifNotNullThen(payStatus, t -> sql.append(" and accountflo0_.state='" + t + "'"));
        ifNotNullThen(opType, t -> sql.append(" and accountflo0_.source='" + t + "'"));
        Query query = entityManager.createNativeQuery(sql.toString());
        BigDecimal money = (BigDecimal) query.getSingleResult();
        return money;
    }


    /**
     * 保存业务流水
     */
    public AccountFlow saveAccountFlow(AccountFlow oldAccountFlow, BigDecimal payedMoney, SourceType sourceType,
                                       Account receiverAccount, BigDecimal reward, ModuleKey.KeyPercentage keyPercentage) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(receiverAccount);
        accountFlow.setAmount(payedMoney);
        accountFlow.setType(PAY_IN);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRecordType(BILL_INCOME);
        accountFlow.setSource(sourceType);
        accountFlow.setState(FlowState.SUCCESS);
        accountFlow.setRelation(keyPercentage);
        setAccountFlowRemark(accountFlow, reward);
        if (isNotNull(oldAccountFlow)) {
            accountFlow.setJob(oldAccountFlow.getJob());
            accountFlow.setJobApplyRecord(oldAccountFlow.getJobApplyRecord());
        }
        accountFlow = accountFlowRepository.save(accountFlow);
        return accountFlow;
    }

    public AccountFlow findTop1ByAccountMemberTokenAndSourceOrderByCreateDate(String userToken, SourceType partnerApply) {
        return accountFlowRepository.findTop1ByAccountMemberTokenAndSourceOrderByCreateDate(userToken, partnerApply);
    }

    public AccountFlow findPartnerFlow(Long partnerApplyId) {
        return accountFlowRepository.findByPartnerApplyIdAndSourceAndStateAndType(partnerApplyId, PARTNER_APPLY, SUCCESS, PAY_OUT);
    }

    public AccountFlow findCourseApplyFlow(CourseApply courseApply, FlowState state) {
        return accountFlowRepository.findByCourseApplyAndSourceAndStateAndType(courseApply, COURSE_APPLY, state, PAY_OUT);
    }

    public AccountFlow findCourseRefundApplyFlow(CourseApply courseApply, CourseRefundApply courseRefundApply, FlowState state) {
        return accountFlowRepository.findByCourseApplyAndCourseRefundApplyAndSourceAndStateAndType(courseApply, courseRefundApply, COURSE_REFUND, state, PAY_IN);
    }

    public AccountFlow findEntrepreneursFlow(Long entrepreneursId) {
        return accountFlowRepository.findByEntrepreneursApplyIdAndTypeAndSourceAndState(entrepreneursId, PAY_OUT, ENTREPRENEUR_APPLY, SUCCESS);
    }

    /**
     * 按照时间段,来源,关键字匹配查询
     */
    public Page<AccountFlow> periodSourceTypeAndKeyWord(Date startTime, Date endTime, SourceType sourceType, String text, BackPageRequest backPageRequest) {
        Specification specification = (rt, query, cb) -> {
            List<Predicate> predicates = getPredicates(startTime, endTime, text, rt, cb, "entrepreneur");

//            Predicate source = rt.get("source").in(isNotNull(sourceType) ? newArrayList(sourceType) : ENTREPRENEURS_REWARD_TYPE);
            Predicate source = rt.get("source").in(isNotNull(sourceType) ? newArrayList(sourceType) : newArrayList(ENTREPRENEUR_AWARD));
            Predicate source1 = cb.and(cb.equal(rt.get("source"), PLATFORM_PROFIT_JOB), rt.get("entrepreneursApply").isNotNull());
            predicates.add(cb.or(source, source1));
            return query.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("receivedDate"), false)).getRestriction();
        };
        return findAll(specification, backPageRequest);
    }

    /**
     * 按照时间段,招聘类型,关键字匹配查询
     */
    public Page<AccountFlow> periodRecruitTypeAndKeyWord(Date startTime, Date endTime, RecruitmentType recruitType, String text, BackPageRequest backPageRequest) {
        Specification<AccountFlow> specification = (rt, query, cb) -> {
            List<Predicate> predicates = getPredicates(startTime, endTime, text, rt, cb, "jobApply");
            Predicate source = rt.get("job").get("recType").in(isNotNull(recruitType) ? newArrayList(recruitType) : ENTREPRENEURS_REWARD_RECRUITTYPE_TYPE);
            predicates.add(source);
            predicates.add(rt.get("source").in(newArrayList(PARTNER_ONCE_AWARD, PARTNER_MONTHLY_AWARD, ENTREPRENEUR_RECOMMEND_AWARD,
                    ENTREPRENEUR_REGISTER_AWARD, PLATFORM_PROFIT_JOB)));
            predicates.add(cb.equal(rt.get("type"), PAY_IN));
            return query.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("updateDate"), false)).getRestriction();
        };
        return findAll(specification, backPageRequest);
    }

    private List<Predicate> getPredicates(Date startTime, Date endTime, String text, Root rt, CriteriaBuilder cb, String type) {
        List<Predicate> predicates = newArrayList();
        ifNotNullThen(startTime, d -> predicates.add(cb.greaterThanOrEqualTo(rt.get("createDate"), d)));
        ifNotNullThen(endTime, d -> predicates.add(cb.lessThanOrEqualTo(rt.get("createDate"), d)));
        if ("entrepreneur".equals(type)) {
            ifNotBlankThen(text, t -> predicates.add(cb.or(
                    cb.like(rt.get("entrepreneursApply").get("member").get("realName"), "%" + t + "%"),
                    cb.like(rt.get("entrepreneursApply").get("member").get("hpAccount"), "%" + t + "%")
            )));
        } else if ("jobApply".equals(type)) {
            ifNotBlankThen(text, t -> predicates.add(cb.like(rt.get("jobApplyRecord").get("receiver").get("realName"), "%" + t + "%")));
        }
        return predicates;
    }

    public BigDecimal countWithdraw(String text, FlowState tableType, Date startTime, Date endTime,
                                    FlowState cashStatus, WithdrawType cashAccount, String type) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT  SUM(accountflo0_.amount) AS col_0_0_  FROM  tp_account_flow accountflo0_ " +
                " LEFT JOIN  sr_withdraw_account withdrawac1_ ON accountflo0_.withdraw_account_id=withdrawac1_.id" +
                " LEFT JOIN  core_member member2_ ON withdrawac1_.member_id=member2_.id ");
        sql.append(" WHERE accountflo0_.record_type= 'BILL_WITHDRAW' ");
        if (type.equals("SCHOOL")) {
            sql.append(" AND member2_.role = 'SCHOOL' ");
        } else if (type.equals("MEMBER")) {
            sql.append(" AND ( member2_.role = 'EMPLOYEE' OR member2_.role IS NULL) ");
        }
        if (tableType == APPROVAL) {
            sql.append(" AND  accountflo0_.state = 'APPROVAL'");
        } else sql.append(" and ( accountflo0_.state in ( 'SUCCESS', 'FAILED' )) ");
        sql.append(" AND accountflo0_.type = '" + PAY_OUT + "'");
        ifNotNullThen(startTime, t -> sql.append(" AND accountflo0_.create_date>'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(endTime, t -> sql.append(" AND accountflo0_.create_date<'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotBlankThen(text, t -> sql.append(" AND (  member2_.real_name like '%" + t + "%' OR withdrawac1_.account like '%" + t + "%' ) "));
        ifNotNullThen(cashStatus, t -> sql.append(" AND accountflo0_.state='" + t + "'"));
        ifNotNullThen(cashAccount, t -> sql.append(" AND withdrawac1_.type='" + t + "'"));
        Query query = entityManager.createNativeQuery(sql.toString());
        BigDecimal money = (BigDecimal) query.getSingleResult();
        return money;
    }

    public Page<AccountFlow> findEarningsList(String userToken, Pageable pageable) {
        Specification<EntrepreneursApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member").get("token"), userToken));
            predicates.add(rb.equal(rt.get("state"), ApplyEnum.SUCCESS));
            return rq.where(toArray(predicates, Predicate.class)).getRestriction();
        };
        List<EntrepreneursApply> list = entrepreneursApplyRepository.findAll(spec);
        ifTrueThrow(list.isEmpty(), TIP_NO_ENTREPRENEURS);

        List<SourceType> sourceTypeList = newArrayList(ENTREPRENEUR_AWARD, ENTREPRENEUR_REGISTER_AWARD, ENTREPRENEUR_RECOMMEND_AWARD);
        Specification<AccountFlow> spec1 = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("account").get("member").get("token"), userToken));
            predicates.add(rt.get("source").in(sourceTypeList));
            predicates.add(rb.equal(rt.get("state"), SUCCESS));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        Page<AccountFlow> earningPage = accountFlowRepository.findAll(spec1, pageable);
        return earningPage;
    }

    public Page<AccountFlow> earningsList(String userToken, Pageable pageable) {
        Specification<EntrepreneursApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member").get("token"), userToken));
            predicates.add(rb.equal(rt.get("state"), ApplyEnum.SUCCESS));
            return rq.where(toArray(predicates, Predicate.class)).getRestriction();
        };
        List<EntrepreneursApply> list = entrepreneursApplyRepository.findAll(spec);
        ifTrueThrow(list.isEmpty(), TIP_NO_ENTREPRENEURS);

        List<SourceType> sourceTypeList = newArrayList(ENTREPRENEUR_AWARD);
        Specification<AccountFlow> spec1 = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("account").get("member").get("token"), userToken));
            predicates.add(rt.get("source").in(sourceTypeList));
            predicates.add(rb.equal(rt.get("state"), SUCCESS));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        Page<AccountFlow> earningPage = accountFlowRepository.findAll(spec1, pageable);
        return earningPage;
    }

    public Page<AccountFlow> partnerEarningsList(String userToken, Pageable pageable) {
        Specification<PartnerApply> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member").get("token"), userToken));
            predicates.add(rb.equal(rt.get("state"), ApplyEnum.SUCCESS));
            return rq.where(toArray(predicates, Predicate.class)).getRestriction();
        };
        List<PartnerApply> list = partnerApplyRepository.findAll(spec);
        ifTrueThrow(list.isEmpty(), TIP_NO_PARTNER_APPLY);

        List<SourceType> sourceTypeList = newArrayList(PARTNER_ONCE_AWARD, PARTNER_MONTHLY_AWARD);
        Specification<AccountFlow> spec1 = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("account").get("member").get("token"), userToken));
            predicates.add(rt.get("source").in(sourceTypeList));
            predicates.add(rb.equal(rt.get("state"), SUCCESS));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        Page<AccountFlow> earningPage = accountFlowRepository.findAll(spec1, pageable);
        return earningPage;
    }

    public void createEntrepreneursAccountFlow(EntrepreneursApply entrepreneursApply, Account account, BigDecimal money, PayType payType) {
        if (money.intValue() > 0) {
            AccountFlow accountFlow = new AccountFlow();
            accountFlow.setEntrepreneursApply(entrepreneursApply);
            accountFlow.setAccount(account);
            accountFlow.setAmount(money);
            accountFlow.setSource(ENTREPRENEUR_AWARD);
            accountFlow.setType(PAY_IN);
            accountFlow.setRemark("创业者收益");
            accountFlow.setRecordType(BILL_INCOME);
            accountFlow.setState(SUCCESS);
            accountFlow.setOrderNo(genSerialNo());
            accountFlow.setPayType(payType);
            accountFlowRepository.save(accountFlow);
        }
    }

    public BigDecimal countEarnings(String userToken, String type) {
        List<SourceType> sourceTypeList = newArrayList(ENTREPRENEUR_AWARD, ENTREPRENEUR_REGISTER_AWARD, ENTREPRENEUR_RECOMMEND_AWARD);
        List<SourceType> sourceTypeList1 = newArrayList(PARTNER_ONCE_AWARD, PARTNER_MONTHLY_AWARD);
        List<AccountFlow> estpList;
        if (type.equals("estpEarnings")) {
            estpList = accountFlowRepository.findByAccountMemberTokenAndRecordTypeAndStateAndSourceIn(userToken, BILL_INCOME, SUCCESS, sourceTypeList);
        } else {
            estpList = accountFlowRepository.findByAccountMemberTokenAndRecordTypeAndStateAndSourceIn(userToken, BILL_INCOME, SUCCESS, sourceTypeList1);
        }
        final BigDecimal[] estpEarnings = {new BigDecimal(0)};
        if (!estpList.isEmpty()) {
            estpList.forEach(e -> estpEarnings[0] = estpEarnings[0].add(e.getAmount()));
        }
        return estpEarnings[0];
    }

    public Page<AccountFlow> platformRevenue(BackPageVo pageVO, Date startTime, Date endTime, String earningsType, String text) {
        Account platformAccount = accountService.findZuesAccount();
        Specification<AccountFlow> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, t -> predicates.add(rb.greaterThan(rt.get("createDate"), t)));
            ifNotNullThen(endTime, t -> predicates.add(rb.lessThan(rt.get("createDate"), t)));
            if ("PARTNER".equals(earningsType)) {
                predicates.add(rt.get("partnerApply").isNotNull());
            } else if ("ENTREPRENEURS".equals(earningsType)) {
                predicates.add(rt.get("entrepreneursApply").isNotNull());
            } else if ("JOB_INCOME".equals(earningsType)) {
                predicates.add(rb.and(rt.get("job").isNotNull(), rb.equal(rt.get("source"), PLATFORM_PROFIT_JOB)));
            } else if ("JOB_PENALTY".equals(earningsType)) {
                predicates.add(rb.and(rt.get("job").isNotNull(), rb.equal(rt.get("source"), DEFAULTS)));
            } else if ("COURSE_APPLY".equals(earningsType)) {
                predicates.add(rb.and(rt.get("courseApply").isNotNull(), rb.equal(rt.get("source"), COURSE_APPLY)));
            } else if ("COURSE_REFUND".equals(earningsType)) {
                predicates.add(rb.and(rt.get("courseRefundApply").isNotNull(), rb.equal(rt.get("source"), COURSE_REFUND)));
            }
            //todo:搜送功能有问题
            ifNotBlankThen(text, t -> predicates.add(rb.or(
                    rb.like(rt.get("partnerApply").get("member").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("entrepreneursApply").get("member").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("job").get("shop").get("owner").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("courseApply").get("member").get("realName"), "%" + t + "%"),
                    rb.like(rt.get("courseRefundApply").get("member").get("realName"), "%" + t + "%")
            )));
            predicates.add(rb.equal(rt.get("recordType"), PLATFORM_INCOME));
            predicates.add(rb.equal(rt.get("type"), PAY_IN));
            predicates.add(rb.equal(rt.get("account").get("id"), platformAccount.getId()));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        return accountFlowRepository.findAll(specification, pageVO.pageRequest());
    }


    public AccountFlow revenueDetail(Long id) {
        return accountFlowRepository.findOne(id);
    }

    public Page<AccountFlow> findAccountFlowPage(BackPageVo pageVo, String text, Date startTime, Date endTime, RecordType billType, Long userId) {
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("account").get("member").get("id"), userId));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("createDate"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("createDate"), e)));
            ifNotNullThen(billType, e -> predicates.add(rb.equal(rt.get("recordType"), billType)));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("remark"), "%" + e + "%")));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        return accountFlowRepository.findAll(spec, pageVo.pageRequest());
    }

    /**
     * 统计审核中的提现记录
     *
     * @Author xiao xue wei
     * @Date 2017/1/6
     */
    public Long countReviewingWithdraw(Date startTime, Date endTime) {
        return accountFlowRepository.count(specInfo(startTime, endTime, BILL_WITHDRAW));
    }

    /**
     * 统计审核中的岗位退款
     *
     * @Author xiao xue wei
     * @Date 2017/1/6
     */
    public Long countReviewingJobRefund(Date startTime, Date endTime) {
        return accountFlowRepository.count(specInfo(startTime, endTime, BILL_REFUND));
    }

    public Specification<AccountFlow> specInfo(Date startTime, Date endTime, RecordType recordType) {
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("recordType"), recordType));
            predicates.add(rb.equal(rt.get("state"), APPROVAL));
            ifTrueThen(recordType == BILL_REFUND, () -> predicates.add(rb.equal(rt.get("source"), JOB_REFUND)));
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("createDate"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("createDate"), e)));
            return rq.where(toArray(predicates, Predicate.class)).getRestriction();
        };
        return spec;
    }

    public BigDecimal countRevenue(Date startTime, Date endTime, String text, String earningsType, String type) {
        Account platformAccount = accountService.findZuesAccount();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(accountflo0_.amount) FROM tp_account_flow accountflo0_ ");
        sql.append(" LEFT JOIN sr_partner_apply partnerapp1_ ON accountflo0_.partner_apply_id = partnerapp1_.id ");
        sql.append(" LEFT JOIN core_member member2_ ON partnerapp1_.member_id = member2_.id ");
        sql.append(" LEFT JOIN sr_entrepreneurs_apply entreprene3_ ON accountflo0_.entrepreneurs_apply_id = entreprene3_.id ");
        sql.append(" LEFT JOIN core_member member4_ ON entreprene3_.member_id = member4_.id ");
        sql.append(" LEFT JOIN sr_job job5_ ON accountflo0_.job_id = job5_.id ");
        sql.append(" LEFT JOIN sr_shop shop6_ ON job5_.shop_id = shop6_.id ");
        sql.append(" LEFT JOIN core_member member7_ ON shop6_.owner_id = member7_.id ");
        sql.append(" LEFT JOIN sr_course_apply courseappl8_ ON accountflo0_.course_apply_id = courseappl8_.id ");
        sql.append(" LEFT JOIN core_member member9_ ON courseappl8_.member_id = member9_.id ");
        sql.append(" LEFT JOIN sr_course_refund_apply courserefu10_ ON accountflo0_.course_refund_apply_id = courserefu10_.id ");
        sql.append(" LEFT JOIN core_member member11_ ON courserefu10_.member_id = member11_.id");
        sql.append(" where accountflo0_.account_id=" + platformAccount.getId());
        sql.append(" and accountflo0_.record_type='" + PLATFORM_INCOME + "'");
        sql.append(" and accountflo0_.type='" + PAY_IN + "'");
        ifNotNullThen(startTime, x -> sql.append(" and accountflo0_.create_date>'" + new java.sql.Date(x.getTime()) + "' "));
        ifNotNullThen(endTime, x -> sql.append(" and accountflo0_.create_date<'" + new java.sql.Date(x.getTime()) + "' "));
//        if ((isBlank(earningsType) && "PARTNER".equals(type)) || (isNotBlank(earningsType) && ("PARTNER".equals(earningsType) || "PARTNER".equals(type)))) {
        if ("PARTNER".equals(type)) {
            sql.append(" and (accountflo0_.partner_apply_id is not null)");
        }
        if ("ENTREPRENEURS".equals(type)) {
            sql.append(" and (accountflo0_.entrepreneurs_apply_id is not null)");
        }
        if ("JOB_INCOME".equals(type)) {
            sql.append(" and (accountflo0_.job_id is not null)");
            sql.append(" and (accountflo0_.source = '" + PLATFORM_PROFIT_JOB + "')");
        }
        if ("JOB_PENALTY".equals(type)) {
            sql.append(" and (accountflo0_.job_id is not null)");
            sql.append(" and (accountflo0_.source = '" + DEFAULTS + "')");
        }
        if ("COURSE_APPLY".equals(type)) {
            sql.append(" and (accountflo0_.course_apply_id is not null)");
            sql.append(" and (accountflo0_.source = '" + COURSE_APPLY + "')");
        }
        if ("COURSE_REFUND".equals(type)) {
            sql.append(" and (accountflo0_.course_apply_id is not null)");
            sql.append(" and (accountflo0_.course_refund_apply_id is not null)");
            sql.append(" and (accountflo0_.source = '" + COURSE_REFUND + "')");
        }
        ifNotBlankThen(text, x -> sql.append("( " +
                " member2_.real_name LIKE '%" + x + "%' " +
                " OR member4_.real_name LIKE '%" + x + "%' " +
                " OR member7_.real_name LIKE '%" + x + "%' " +
                " OR member9_.real_name LIKE '%" + x + "%' " +
                " OR member11_.real_name LIKE '%" + x + "%' " +
                " )"));
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    //同一笔创业者收益钱的去向
    public List<AccountFlow> findEntrepreneursFlowList(EntrepreneursApply entrepreneursApply) {
        return accountFlowRepository.findByEntrepreneursApplyIdAndSource(entrepreneursApply.getId(), ENTREPRENEUR_APPLY);
    }

    public List<AccountFlow> findJobIncomeFlowList(Job job) {
        return accountFlowRepository.findByJobIdAndSourceIn(job.getId(), newArrayList(JOB_NEW, JOB_ADD, JOB_CUT, JOB_RENEW, JOB_RESIGN, JOB_REFUND));
    }

    /**
     * 产生平台出账流水
     */
    public AccountFlow savePlatformFlow(JobApplyRecord jobApplyRecord, Job job, Account zuesAccount, BigDecimal amount,
                                        SourceType sourceType, RecordType recordType, CourseApply courseApply, CourseRefundApply courseRefundApply, BigDecimal reward) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(zuesAccount);
        accountFlow.setAmount(amount);
        accountFlow.setType(PAY_OUT);
        accountFlow.setRecordType(recordType);
        accountFlow.setSource(sourceType);
        accountFlow.setState(SUCCESS);
        accountFlow.setOrderNo(genSerialNo());
        ifNotNullThen(jobApplyRecord, t -> accountFlow.setJobApplyRecord(t));
        ifNotNullThen(job, t -> accountFlow.setJob(t));
        setAccountFlowRemark(accountFlow, reward);
        ifNotNullThen(courseApply, t -> accountFlow.setCourseApply(t));
        ifNotNullThen(courseRefundApply, t -> accountFlow.setCourseRefundApply(t));
        return save(accountFlow);
    }

    /**
     * 产生店铺账户收入退押金流水
     */
    public String saveShopOwnerFlow(JobApplyRecord jobApplyRecord, Account shopAccount, BigDecimal remainAmount) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(shopAccount);
        accountFlow.setAmount(remainAmount);
        accountFlow.setType(PAY_IN);
        accountFlow.setRecordType(BILL_REFUND);
        accountFlow.setSource(JOB_RESIGN);
        accountFlow.setRemark(JOB_RESIGN.getRemark());
        accountFlow.setState(SUCCESS);
        ifNotNullThen(jobApplyRecord, x -> {
            accountFlow.setJobApplyRecord(x);
            accountFlow.setJob(x.getJob());
        });
        accountFlow.setOrderNo(genSerialNo());
        save(accountFlow);
        return "success";
    }

    /**
     * 保存平台收益流水---岗位续费违约金
     *
     * @Author xiao xue wei
     * @Date 2017/1/12
     */
    public String savePlatformEarningsFlow(JobApplyRecord jobApplyRecord, Account platformAccount, BigDecimal breach) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(platformAccount);
        accountFlow.setAmount(breach);
        accountFlow.setType(PAY_IN);
        accountFlow.setRecordType(PLATFORM_INCOME);
        accountFlow.setSource(DEFAULTS);
        accountFlow.setRemark(DEFAULTS.getRemark());
        accountFlow.setState(SUCCESS);
        ifNotNullThen(jobApplyRecord.getJob(), x -> accountFlow.setJob(x));
        accountFlow.setOrderNo(genSerialNo());
        save(accountFlow);
        return "success";
    }

    /**
     * 平台总收入/总收益统计
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalPlatformIn(Date startTime, Date endTime, RecordType recordType, SourceType source, List<SourceType> sourceList) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(amount) FROM tp_account_flow ");
        if (sourceList != null) {
            sql.append(" WHERE source in (");
            sourceList.forEach(s -> sql.append("'" + s + "',"));
            sql.replace(sql.lastIndexOf(","), sql.lastIndexOf(",") + 1, "");
            sql.append(") ");
        } else {
            sql.append(" WHERE source = '" + source + "' ");
        }
        sql.append(" AND record_type = '" + recordType + "'");
        ifNotNullThen(startTime, date -> sql.append(" AND create_date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND create_date <'" + new java.sql.Date(date.getTime()) + "' "));
        System.out.println(sql.toString());
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 所有用户账户可提现余额
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalMemberAccount(Date startTime, Date endTime, String type) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(a.balance- a.freezing_amount) FROM tp_account a LEFT JOIN core_member m ON a.member_id = m.id WHERE a.zues ='NO'");
        ifNotNullThen(startTime, date -> sql.append(" AND a.create_date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND a.create_date <'" + new java.sql.Date(date.getTime()) + "' "));
        if ("SCHOOL".equals(type)) {
            sql.append(" AND m.role = 'SCHOOL' ");
        } else sql.append(" AND ( m.role <> 'SCHOOL' OR m.role IS NULL )");
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 所有用户账户不可提现余额
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalMemberAccountUnWithdraw(Date startTime, Date endTime, String type) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(a.freezing_amount) FROM tp_account a LEFT JOIN core_member m ON a.member_id = m.id WHERE a.zues ='NO'");
        ifNotNullThen(startTime, date -> sql.append(" AND a.create_date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND a.create_date <'" + new java.sql.Date(date.getTime()) + "' "));
        if ("SCHOOL".equals(type)) {
            sql.append(" AND m.role = 'SCHOOL' ");
        } else sql.append(" AND m.role <> 'SCHOOL' ");
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 所有用户提现总金额
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalBillWithdraw(Date startTime, Date endTime) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(amount) FROM tp_account_flow flow ");
        sql.append(" LEFT JOIN sr_withdraw_account w ON flow.withdraw_account_id = w.id  ");
        sql.append(" LEFT JOIN core_member m ON w.member_id = m.id ");
        sql.append(" WHERE record_type ='BILL_WITHDRAW' ");
        sql.append(" AND m.role <> 'SCHOOL'");
        ifNotNullThen(startTime, date -> sql.append(" AND create_date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND create_date <'" + new java.sql.Date(date.getTime()) + "' "));
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 所有学校用户 提现总金额
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalBillWithdrawOfSchool(Date startTime, Date endTime) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(amount) FROM tp_account_flow flow ");
        sql.append("LEFT JOIN sr_withdraw_account w ON flow.withdraw_account_id = w.id  ");
        sql.append("LEFT JOIN core_member m ON w.member_id = m.id ");
        sql.append("WHERE record_type ='BILL_WITHDRAW' ");
        sql.append("AND m.role = 'SCHOOL'");
        ifNotNullThen(startTime, date -> sql.append(" AND flow.create_date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND flow.create_date <'" + new java.sql.Date(date.getTime()) + "' "));
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 预付一次性悬赏招聘总金额
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalJobApplyRecordOfOnce(Date startTime, Date endTime, RecruitmentType recType) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(reward*epm_count) FROM sr_job WHERE rec_type = '" + recType + "' AND state IN('NORMAL', 'PAUSE', 'STOP', 'FROZEN', 'DELETE')");
        ifNotNullThen(startTime, date -> sql.append(" AND date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND date <'" + new java.sql.Date(date.getTime()) + "' "));
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 预付 按月悬赏招聘总金额
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalJobApplyRecordOfMonthly(Date startTime, Date endTime, RecruitmentType recType) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(reward*(epm_count*4)) FROM sr_job WHERE rec_type = '" + recType + "' AND state IN('NORMAL', 'PAUSE', 'STOP', 'FROZEN', 'DELETE')");
        ifNotNullThen(startTime, date -> sql.append(" AND date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND date <'" + new java.sql.Date(date.getTime()) + "' "));
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 按月悬赏招聘 押金总金额
     *
     * @Author mu.jie
     * @Date 2017/1/16
     */
    public BigDecimal totalJobApplyRecordOfMonthlyDeposit(Date startTime, Date endTime) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SUM(reward*(epm_count*3)) FROM sr_job WHERE rec_type = 'MONTHLY' AND id NOT IN (SELECT job_id FROM sr_hp_apply WHERE state ='IN_REVIEW' AND type ='JOB_RESIGN')");
        sql.append(" AND state IN('NORMAL', 'PAUSE', 'STOP', 'FROZEN', 'DELETE')");
        ifNotNullThen(startTime, date -> sql.append(" AND date >'" + new java.sql.Date(date.getTime()) + "' "));
        ifNotNullThen(endTime, date -> sql.append(" AND date <'" + new java.sql.Date(date.getTime()) + "' "));
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    /**
     * 判断是否得到车旅费，且车旅费是否过期
     *
     * @Author mu.jie
     * @Date 2017/1/17
     */
    public Boolean findCarFeeAccountFlow(String userToken) {
        AccountFlow accountFlow = accountFlowRepository.findTop1ByAccountMemberTokenAndSourceOrderByCreateDate(userToken, EMPLOYEE_CAR_FEE);
        if (isNull(accountFlow)) {
            return true;
        }
        if (addMonths(accountFlow.getCreateDate(), 12).getTime() > new Date().getTime()) {
            return true;
        } else {
            return false;
        }
    }

    public Page<AccountFlow> findEntreFeePage(Pageable pageable, String text, EntrepreneursType estpType, Long userId) {
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("account").get("member").get("id"), userId));
            predicates.add(rb.equal(rt.get("recordType"), BILL_INCOME));
            predicates.add(rb.equal(rt.get("source"), ENTREPRENEUR_AWARD));
            ifNotNullThen(estpType, e -> predicates.add(rb.equal(rt.get("entrepreneursApply").get("type"), e)));
            ifNotBlankThen(text, e -> {
                String textStr = "%" + e + "%";
                predicates.add(rb.or(rb.like(rt.get("entrepreneursApply").get("member").get("realName"), textStr),
                        rb.like(rt.get("entrepreneursApply").get("member").get("mobile"), textStr),
                        rb.like(rt.get("entrepreneursApply").get("member").get("hpAccount"), textStr)));
            });
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        return accountFlowRepository.findAll(spec, pageable);
    }

    public BigDecimal countCourseApplyFlowsMoneys(CourseSignUp courseSignUp) {
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("type"), PAY_IN));
            predicates.add(rb.equal(rt.get("recordType"), BILL_INCOME));
            predicates.add(rb.equal(rt.get("source"), COURSE_APPLY));
            predicates.add(rb.equal(rt.get("courseApply").get("course").get("id"), courseSignUp.getCourse().getId()));
            Date date = new Date(courseSignUp.getDate().getTime());
            Date date1 = new Date(courseSignUp.getDate().getTime() + (24 * 3600 * 1000));
            predicates.add(rb.greaterThanOrEqualTo(rt.get("courseApply").get("trainDate"), date));
            predicates.add(rb.lessThan(rt.get("courseApply").get("trainDate"), date1));
            predicates.add(rb.equal(rt.get("account").get("member").get("id"), courseSignUp.getCourse().getSchool().getMember().getId()));
            return rq.where(toArray(predicates, Predicate.class)).getRestriction();
        };
        List<AccountFlow> list = accountFlowRepository.findAll(spec);
        BigDecimal moneys = new BigDecimal(0);
        if (!list.isEmpty()) list.forEach(accountFlow -> moneys.add(accountFlow.getAmount()));
        return moneys;
    }

    public void updateCourseApplyAccountFlow(Long id, FlowState payStatus, Date payTime, PayOfflineType payPathWay, String bank, String bankFlow, String remark) {
        AccountFlow one = accountFlowRepository.findOne(id);
        one.setState(payStatus);
        one.setUpdateDate(payTime);

    }

    public Page<AccountFlow> findSchoolWithdrawAccountFlow(BackPageVo backPageVo, String text, Member member) {
        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            if (member.getRole() == SCHOOL)
                predicates.add(rb.equal(rt.get("account").get("member").get("id"), member.getId()));
            predicates.add(rb.equal(rt.get("recordType"), BILL_WITHDRAW));
            predicates.add(rb.equal(rt.get("source"), SCHOOL_WITHDRAW));
            predicates.add(rb.equal(rt.get("type"), PAY_IN));
            ifNotBlankThen(text, t -> predicates.add(rb.like(rt.get("withdrawAccount").get("school").get("name"), "%" + t + "%")));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();
        };
        return accountFlowRepository.findAll(spec, backPageVo.pageRequest());
    }

    public AccountFlow findHpApplyFlow(HpApply one) {
        return accountFlowRepository.findByTypeAndRecordTypeAndSourceAndHpApplyId(PAY_IN, BILL_REFUND, JOB_REFUND, one.getId());
    }

    public AccountFlow findReturnDepositHpApplyFlow(HpApply one) {
        return accountFlowRepository.findByTypeAndRecordTypeAndSourceAndHpApplyId(PAY_IN, BILL_REFUND, JOB_RESIGN, one.getId());
    }

    public BigDecimal countCourseApplyPlatform(String text, Date startTime, Date endTime, Account platform) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(accountflo0_.amount) AS col_0_0_ FROM tp_account_flow accountflo0_ ");
        sql.append(" LEFT JOIN sr_course_apply courseappl1_ ON accountflo0_.course_apply_id = courseappl1_.id ");
        sql.append(" LEFT JOIN core_member member2_ ON courseappl1_.member_id = member2_.id ");
        sql.append(" LEFT JOIN sr_course course6_ ON courseappl1_.course_id = course6_.id ");
        sql.append(" WHERE accountflo0_.type ='" + PAY_IN + "' ");
        sql.append(" AND accountflo0_.account_id = " + platform.getId());
        sql.append(" AND (accountflo0_.state IN('" + SUCCESS + "', '" + FAILED + "'))  AND accountflo0_.source ='" + COURSE_APPLY + "' ");
        ifNotBlankThen(text, t -> sql.append(" AND (member2_.real_name LIKE '%" + t + "%' OR member2_.mobile LIKE '%" + t + "%'  OR course6_. NAME LIKE '%" + t + "%' ) "));
        ifNotNullThen(startTime, t -> sql.append(" AND accountflo0_.create_date>'" + new java.sql.Date(t.getTime()) + "'"));
        ifNotNullThen(endTime, t -> sql.append(" AND accountflo0_.create_date<'" + new java.sql.Date(t.getTime()) + "'"));
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }

    public BigDecimal countCourseApplySchool(String text, Date startTime, Date endTime, Account platform) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(accountflo0_.amount) AS col_0_0_ FROM tp_account_flow accountflo0_ ");
        sql.append(" LEFT JOIN sr_course_apply courseappl1_ ON accountflo0_.course_apply_id = courseappl1_.id ");
        sql.append(" LEFT JOIN core_member member2_ ON courseappl1_.member_id = member2_.id ");
        sql.append(" LEFT JOIN sr_course course6_ ON courseappl1_.course_id = course6_.id ");
        sql.append(" WHERE accountflo0_.type ='" + PAY_IN + "' ");
        sql.append(" AND accountflo0_.account_id <> " + platform.getId());
        sql.append(" AND (accountflo0_.state IN('" + SUCCESS + "', '" + FAILED + "'))  AND accountflo0_.source ='" + COURSE_APPLY + "' ");
        ifNotBlankThen(text, t -> sql.append(" AND (member2_.real_name LIKE '%" + t + "%' OR member2_.mobile LIKE '%" + t + "%'  OR course6_. NAME LIKE '%" + t + "%' ) "));
        ifNotNullThen(startTime, t -> sql.append(" AND accountflo0_.create_date>'" + new java.sql.Date(t.getTime()) + "' "));
        ifNotNullThen(endTime, t -> sql.append(" AND accountflo0_.create_date<'" + new java.sql.Date(t.getTime()) + "' "));
        System.out.println(sql.toString());
        Query query = entityManager.createNativeQuery(sql.toString());
        return (BigDecimal) query.getSingleResult();
    }
}
