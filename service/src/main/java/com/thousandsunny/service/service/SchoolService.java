package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.domain.service.SmsService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.model.WithdrawAccount;
import com.thousandsunny.service.repository.SchoolRepository;
import com.thousandsunny.service.repository.WithdrawAccountRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.domain.service.AccountService;
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
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.SmsType.WITHDRAW_VERIFY;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.service.ModuleKey.WithdrawType.*;
import static com.thousandsunny.service.ModuleTips.TIP_NO_SCHOOL;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_IN;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.APPROVAL;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.*;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_WITHDRAW;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.SCHOOL_WITHDRAW;
import static com.thousandsunny.thirdparty.ModuleTips.*;
import static org.apache.jackrabbit.util.Text.md5;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

/**
 * Created by 13336 on 2017/2/14.
 */
@Service
public class SchoolService extends BaseService<School> {
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private SmsService smsService;
    @Autowired
    private WithdrawAccountRepository withdrawAccountRepository;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountFlowRepository accountFlowRepository;

    public Page<School> findSchoolPage(Pageable pageable, String keyword, Long provinceId, Long cityId, Long areaId) {
        Specification<School> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotBlankThen(keyword, e -> predicates.add(rb.like(rt.get("name"), "%" + e + "%")));
            ifNotNullThen(provinceId, e -> predicates.add(rb.equal(rt.get("province").get("id"), e)));
            ifNotNullThen(cityId, e -> predicates.add(rb.equal(rt.get("city").get("id"), e)));
            ifNotNullThen(areaId, e -> predicates.add(rb.equal(rt.get("area").get("id"), e)));
            return rq.where(predicates.toArray(new Predicate[]{}))
                    .orderBy(new OrderImpl(rt.get("isPartSchool"), false), (new OrderImpl(rt.get("date"), false))).getRestriction();
        };
        return schoolRepository.findAll(spec, pageable);
    }

    public List<School> findSchoolList() {
        return schoolRepository.findAll();
    }

    public void withdrawlPay(Member member, Long withdrawId, String authCode, BigDecimal amount, String password) {
        smsService.validateReceiverAndCode(member.getMobile(), authCode, WITHDRAW_VERIFY);
        Account account = accountService.findByMemberToken(member.getToken());
        ifNullThrow(account, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        ifTrueThrow(account.getPayPassword() == null && !account.getPayPassword().equals(md5(md5(password))), TIP_PWD_WRONG);
        ifTrueThrow(account.getBalance().subtract(account.getFreezingAmount()).compareTo(amount) < 0, TIPS_SCHOOL_ACCOUNT_BALANCE_ENOUGH);
        WithdrawAccount withdrawAccount = withdrawAccountRepository.findOne(withdrawId);
        ifNullThrow(withdrawAccount, TIP_WITHDRAW_ACCOUNT_NOT_EXIST);
        createAccountFlow(withdrawAccount, account, amount);//创建提现流水
    }

    /**
     * 创建学校账户提现流水
     *
     * @Author mu.jie
     * @Date 2017/2/23
     */
    private AccountFlow createAccountFlow(WithdrawAccount withdrawAccount, Account account, BigDecimal amount) {
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccount(account);
        accountFlow.setWithdrawAccount(withdrawAccount);
        accountFlow.setAmount(amount);
        accountFlow.setType(PAY_IN);//对于学校账户来说是进账
        accountFlow.setRecordType(BILL_WITHDRAW);
        accountFlow.setSource(SCHOOL_WITHDRAW);
        if (withdrawAccount.getType() == WITHDRAW_ACCOUNT_ALIPAY) {
            accountFlow.setPayType(PAY_BY_ALIPAY);
        } else if (withdrawAccount.getType() == WITHDRAW_ACCOUNT_WX) {
            accountFlow.setPayType(PAY_BY_WX);
        } else if (withdrawAccount.getType() == WITHDRAW_ACCOUNT_BANK_CARD) {
            accountFlow.setPayType(PAY_OFFLINE);
        }
        accountFlow.setState(APPROVAL);
        accountFlow.setOrderNo(genSerialNo());
        accountFlow.setRemark(SCHOOL_WITHDRAW.getRemark());
        return accountFlowRepository.save(accountFlow);
    }

    public School findMemberSchool(Member member) {
        School school = schoolRepository.findByMemberId(member.getId());
        ifNullThrow(school, TIP_NO_SCHOOL);
        return school;
    }

    public Long countSchoolInfo(Date startTime, Date endTime, String type) {
        Specification<School> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("date"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("date"), e)));
            if ("partSchool".equals(type)) predicates.add(rb.equal(rt.get("isPartSchool"), YES));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        return schoolRepository.count(spec);
    }
}
