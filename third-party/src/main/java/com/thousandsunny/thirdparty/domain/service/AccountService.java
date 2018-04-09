package com.thousandsunny.thirdparty.domain.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.domain.repository.AccountFreezingRecordRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFreezingRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateUtils.addDays;

/**
 * 如果这些代码有用，那它们是guitarist在09/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class AccountService extends BaseService<Account> {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountFreezingRecordRepository accountFreezingRecordRepository;

    public Account findByMemberToken(String token) {
        return accountRepository.findByMemberToken(token);
    }

    public Account findByMemberMobile(String mobile) {
        return accountRepository.findByMemberMobile(mobile);
    }

    public Account findZuesAccount() {
        return accountRepository.findByZues(YES);
    }

    /**
     * 会员账户账户余额付钱到系统账户
     */
    public void memberAccountPayMoney(Account memberAccount, BigDecimal payAmount) {
        memberAccount.setBalance(memberAccount.getBalance().subtract(payAmount));
        memberAccount.setTotal(memberAccount.getTotal().subtract(payAmount));
        save(memberAccount);

        Account zuesAccount = findZuesAccount();
        zuesAccount.setBalance(zuesAccount.getBalance().add(payAmount));
        zuesAccount.setTotal(zuesAccount.getTotal().add(payAmount));
        save(zuesAccount);
    }

    /**
     * 系统账户账户转钱到会员账户,金额不冻结
     */
    public void zuesAccountRefundMoney(Account memberAccount, BigDecimal payAmount) {
        memberAccount.setBalance(memberAccount.getBalance().add(payAmount));
        memberAccount.setTotal(memberAccount.getTotal().add(payAmount));
        save(memberAccount);

        Account zuesAccount = findZuesAccount();
        zuesAccount.setBalance(zuesAccount.getBalance().subtract(payAmount));
        zuesAccount.setTotal(zuesAccount.getTotal().subtract(payAmount));
        save(zuesAccount);
    }

    /**
     * 系统账户账户转钱到会员账户,将金额冻结,不可提现
     *
     * @Author mu.jie
     * @Date 2017/2/27
     */
    public void freezeZuesAccountRefundMoney(Account memberAccount, BigDecimal payAmount, String remark) {
        memberAccount.setBalance(memberAccount.getBalance().add(payAmount));
        memberAccount.setTotal(memberAccount.getTotal().add(payAmount));
        memberAccount.setFreezingAmount(memberAccount.getFreezingAmount().add(payAmount));
        save(memberAccount);

        Account zuesAccount = findZuesAccount();
        zuesAccount.setBalance(zuesAccount.getBalance().subtract(payAmount));
        zuesAccount.setTotal(zuesAccount.getTotal().subtract(payAmount));
        save(zuesAccount);
        //产生一条冻结记录
        AccountFreezingRecord record = new AccountFreezingRecord();
        record.setAmount(payAmount);
        record.setAccount(memberAccount);
        record.setRemark(remark);
        record.setUnfreezeDate(addDays(new Date(), 15));
        accountFreezingRecordRepository.save(record);
    }
}
