package com.thousandsunny.service.service;

import com.google.common.collect.Lists;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.CloudFileService;
import com.thousandsunny.core.domain.service.SmsService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.RenewalsRecord;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.model.WithdrawAccount;
import com.thousandsunny.service.repository.RenewalsRecordRepository;
import com.thousandsunny.service.repository.SchoolRepository;
import com.thousandsunny.service.repository.WithdrawAccountRepository;
import com.thousandsunny.thirdparty.ModuleKey;
import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.AccountEnum.SCHOOL;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.SmsType.WITHDRAW_VERIFY;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.APPROVAL;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.FAILED;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.MEMBER_WITHDRAW;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.*;
import static java.util.Objects.isNull;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.jackrabbit.util.Text.md5;

/**
 * Created by admin on 2016/11/7.
 */
@Service
public class WithdrawAccountService extends BaseService<WithdrawAccount> {
    @Autowired
    private WithdrawAccountRepository withdrawAccountRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AccountFlowRepository accountFlowRepository;
    @Autowired
    private RenewalsRecordRepository renewalsRecordRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private SmsService smsService;
    @Autowired
    private CloudFileService cloudFileService;
    @Autowired
    private SchoolRepository schoolRepository;

    private static ArrayList<ModuleKey.RecordType> recordTypes = newArrayList(BILL_WITHDRAW, BILL_REFUND);
    private static ArrayList<ModuleKey.RecordType> recordTypes1 = newArrayList(BILL_RECHARGE, BILL_PAY_ONLINE, BILL_PAY_OFFLINE, BILL_INCOME);
    private static ArrayList<ModuleKey.FlowState> flowStates = newArrayList(SUCCESS, APPROVAL, FAILED);

    public List<WithdrawAccount> getWithdrawAccounts(String userToken) {
        return withdrawAccountRepository.findByMemberTokenAndIsDelete(userToken, NO);
    }

    public String delete(String userToken, Long id) {
        WithdrawAccount withdrawAccount = withdrawAccountRepository.findByMemberTokenAndIdAndIsDelete(userToken, id, NO);
        ifNullThrow(withdrawAccount, TIP_NO_WITHDRAW_ACCOUNT);
        withdrawAccount.setIsDelete(YES);
        return "success";
    }

    public String edit(String userToken, WithdrawAccount wa) {
        ifNotNullThen(wa.getIdCard(), f -> wa.setIdCard(cloudFileService.save(f)));
        ifNotNullThen(wa.getHalf(), h -> wa.setHalf(cloudFileService.save(h)));
        if (isNull(wa.getId())) {
            Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
            ifNullThrow(member, TIP_NO_MEMBER);
            wa.setMember(member);
            withdrawAccountRepository.save(wa);
        } else {
            WithdrawAccount withdrawAccount = withdrawAccountRepository.findByMemberTokenAndIdAndIsDelete(userToken, wa.getId(), NO);
            ifNullThrow(withdrawAccount, TIP_NO_WITHDRAW_ACCOUNT);
            ifNotNullThen(wa.getType(), withdrawAccount::setType);
            ifNotNullThen(wa.getName(), withdrawAccount::setName);
            ifNotNullThen(wa.getAccount(), withdrawAccount::setAccount);
            ifNotNullThen(wa.getBank(), withdrawAccount::setBank);
            ifNotNullThen(wa.getBranchBank(), withdrawAccount::setBranchBank);
            withdrawAccountRepository.save(withdrawAccount);

        }
        return "success";
    }

    public Page<AccountFlow> listFlows(String userToken, Pageable pageable) {
//        List<PayType> payTypes = newArrayList(PAY_BY_WX, PAY_BY_ALIPAY, PAY_BY_BALANCE);

        Specification<AccountFlow> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            predicates.add(rb.equal(rt.get("account").get("member").get("token"), userToken));
            predicates.add(rb.isNotNull(rt.get("source")));
            predicates.add(rb.or(
                    rb.and(rt.get("recordType").in(recordTypes), rt.get("state").in(flowStates)),
                    rb.and(rt.get("recordType").in(recordTypes1), rb.equal(rt.get("state"), SUCCESS))
            ));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createDate"), false)).getRestriction();

        };
        return accountFlowRepository.findAll(spec, pageable);
    }

    public AccountFlow showFlowDetails(String userToken, Long id) {
        AccountFlow accountFlow = accountFlowRepository.findByAccountMemberTokenAndId(userToken, id);
        ifNullThrow(accountFlow, TIP_NO_ACCOUNT_FLOW);
        return accountFlow;
    }

    public Page<RenewalsRecord> showRecords(String userToken, Long jobId, Pageable pageable) {
        return renewalsRecordRepository.findByAccountFlowAccountMemberTokenAndAccountFlowJobId(userToken, jobId, pageable);
    }

    public String withdrawApply(String userToken, BigDecimal amount, String payPassword, Long withdrawAccountId, Integer code) {
        //判断用户
        Member member = memberRepository.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);

        //判断提现账户
        WithdrawAccount withdrawAccount = withdrawAccountRepository.findByMemberTokenAndIdAndIsDelete(userToken, withdrawAccountId, NO);
        ifNullThrow(withdrawAccount, TIP_NO_WITHDRAW_ACCOUNT);

        //判断提现验证码
        smsService.validateReceiverAndTypeAndCode(member.getMobile(), WITHDRAW_VERIFY, code);

        //判断会员账户支付密码
        Account account = accountRepository.findByMemberToken(userToken);
        ifNullThrow(account, TIP_NO_ACCOUNT);
        ifFalseThrow(account.getPayPassword().equals(md5(md5(payPassword))), TIP_ERROR_PAY_PASSWORD);

        //判断提现账户可用金额
        ifFalseThrow(account.getBalance().doubleValue() >= amount.doubleValue(), TIP_NO_ENOUGH_BALANCE);

        //资金流水表新增保存一条提现申请流水记录，提现状态为待审核
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(account);
        accountFlow.setAmount(amount);
        accountFlow.setWithdrawAccount(withdrawAccount);
        accountFlow.setType(PAY_OUT);
        accountFlow.setRemark("提现");
        accountFlow.setRecordType(BILL_WITHDRAW);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setState(APPROVAL);
        accountFlow.setSource(MEMBER_WITHDRAW);
        accountFlowRepository.save(accountFlow);
        BigDecimal freezingAmount = account.getFreezingAmount();
        account.setFreezingAmount(freezingAmount.add(amount));
        BigDecimal balance = account.getBalance();
        account.setBalance(balance.subtract(amount));
        account.setTotal(account.getTotal().subtract(amount));
        accountRepository.save(account);
        return "success";
    }

    public WithdrawAccount findByIdAndMemberToken(Long id, String userToken) {
        return withdrawAccountRepository.findByIdAndMemberTokenAndIsDelete(id, userToken, NO);
    }

    public BigDecimal countAllWithdraw(String userToken) {
        List<AccountFlow> withdrawList = accountFlowRepository.findByWithdrawAccountMemberTokenAndRecordTypeAndState(userToken, BILL_WITHDRAW, SUCCESS);
        BigDecimal allWithdraw = new BigDecimal(0);
        if (!withdrawList.isEmpty())
            withdrawList.forEach(e -> allWithdraw.add(e.getAmount()));
        return allWithdraw;
    }

    public Page<WithdrawAccount> findHisWithdraw(Long userId, BackPageVo pageVo) {
        return withdrawAccountRepository.findByMemberIdAndIsDelete(userId, NO, pageVo.pageRequest());
    }


    public Page<WithdrawAccount> findSchoolWithDrawalList(Member member, BackPageVo backPageVo, String text) {
        Specification<WithdrawAccount> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            if (member.getRole() == SCHOOL)
                predicates.add(rb.equal(rt.get("member"), member));
            else predicates.add(rt.get("school").isNotNull());
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if (isNotBlank(text))
                predicates.add(rb.like(rt.get("school").get("name"), "%" + text + "%"));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("date"), false)).getRestriction();
        };
        return withdrawAccountRepository.findAll(spec, backPageVo.pageRequest());
    }

    public void addOrEditWithdrawal(Member member, WithdrawAccount withdrawAccount, String idCardImg, String halfImg) {
        ifFalseThrow(member.getRole() == SCHOOL, TIP_NO_AUTHORITY);
        School school = schoolRepository.findByMemberId(member.getId());
        ifNullThrow(school, TIP_NO_SCHOOL);
        if (isNotBlank(idCardImg)) {
            CloudFile cloudFile1 = new CloudFile();
            cloudFile1.setPath(idCardImg);
            withdrawAccount.setIdCard(cloudFileService.save(cloudFile1));
        }
        if (isNotBlank(halfImg)) {
            CloudFile cloudFile2 = new CloudFile();
            cloudFile2.setPath(halfImg);
            withdrawAccount.setHalf(cloudFileService.save(cloudFile2));
        }
        if (isNull(withdrawAccount.getId())) {
            withdrawAccount.setMember(member);
            withdrawAccount.setSchool(school);
            withdrawAccountRepository.save(withdrawAccount);
        } else {
            WithdrawAccount old = withdrawAccountRepository.findOne(withdrawAccount.getId());
            ifNotNullThen(withdrawAccount.getBank(), old::setBank);
            ifNotNullThen(withdrawAccount.getAccount(), old::setAccount);
            ifNotNullThen(withdrawAccount.getBranchBank(), old::setBranchBank);
            ifNotNullThen(withdrawAccount.getHalf(), old::setHalf);
            ifNotNullThen(withdrawAccount.getIdCard(), old::setIdCard);
            ifNotNullThen(withdrawAccount.getType(), old::setType);
            ifNotNullThen(withdrawAccount.getIdCardNo(), old::setIdCardNo);
            old.setSchool(school);
            old.setMember(member);
            withdrawAccountRepository.save(old);
        }
    }

    public List<WithdrawAccount> findByMemberToken(String userToken) {
        return withdrawAccountRepository.findByMemberTokenAndIsDelete(userToken, NO);
    }
}