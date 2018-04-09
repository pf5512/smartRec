package com.thousandsunny.thirdparty.domain.service;

import com.pingplusplus.model.Charge;
import com.pingplusplus.model.Event;
import com.pingplusplus.model.Transfer;
import com.thousandsunny.common.exception.BaseException;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.domain.repository.TpAccountApplyRecordRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.domain.repository.ThirdPartyPayAccountRepository;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.TpAccountApplyRecord;
import com.thousandsunny.thirdparty.model.AccountFlow;
import com.thousandsunny.thirdparty.model.ThirdPartyPayAccount;
import com.thousandsunny.thirdparty.pingpp.ChargeService;
import com.thousandsunny.thirdparty.pingpp.TransferService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.function.BiConsumer;

import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_IN;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.APPROVAL;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_BY_ALIPAY;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_BY_WX;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_BY_WX_PUB;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.ALIPAY_PC_DIRECT;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_RECHARGE;
import static com.thousandsunny.thirdparty.ModuleTips.*;
import static com.thousandsunny.thirdparty.pingpp.PingppUtil.*;
import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

/**
 * Created by guitarist on 7/14/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */

@Service
public class TpAccountApplyRecordService extends BaseService<TpAccountApplyRecord> {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ThirdPartyPayAccountRepository payAccountRepository;
    @Autowired
    private TpAccountApplyRecordRepository accountApplyRecordRepository;
    @Autowired
    private AccountFlowRepository accountFlowRepository;
    @Autowired
    private ChargeService chargeService;
    @Autowired
    private TransferService transferService;

    /**
     * 创建体现对象
     */
    public Transfer createWithdraw(Transfer transfer, String userToken) {
        String orderNo = "";
        BigDecimal amount = new BigDecimal(transfer.getAmount()).divide(new BigDecimal(100), ROUND_HALF_EVEN);
        if (transfer.getChannel().equals("alipay"))
            orderNo = withdrawApply(userToken, amount, PAY_BY_ALIPAY);
        else if (transfer.getChannel().equals("wx"))
            orderNo = withdrawApply(userToken, amount, PAY_BY_WX);
        transfer.setOrderNo(orderNo);
        return transferService.createTransfer(transfer);
    }

    /**
     * 创建体现对象_具体
     */
    public String withdrawApply(String userToken, BigDecimal amount, PayType payType) {
        TpAccountApplyRecord applyRecord = new TpAccountApplyRecord();
        Member member = memberRepository.findByToken(userToken);
        Account account = accountRepository.findByMemberId(member.getId());
        ifNullThrow(account, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        //可提现金额
        BigDecimal d = account.getBalance().subtract(account.getFreezingAmount());
        ifTrueThrow(d.compareTo(amount) == -1, TIP_BALANCE_NOT_ENOUGH);
        ThirdPartyPayAccount thirdPartyPayAccount = payAccountRepository.findByOwnerAndActive(member, YES);
        if (!isNull(thirdPartyPayAccount)) {
            applyRecord.setAccount(account);
            applyRecord.setThirdPartyPayAccount(thirdPartyPayAccount);
            applyRecord.setAmount(amount);
            applyRecord.setMember(member);
            applyRecord.setPayType(payType);
            applyRecord.setOrderNo(genSerialNo());
            applyRecord.setCreateDate(new Date());
            applyRecord.setState(APPROVAL);
            accountApplyRecordRepository.save(applyRecord);
        } else {
            throw new BaseException(TIP_ACCOUNT_NOT_ACTIVE);
        }
        //修改冻结金额
        account.setFreezingAmount(account.getFreezingAmount().add(amount));
        accountRepository.save(account);
        return applyRecord.getOrderNo();
    }


    /**
     * 创建充值对象
     */
    public Charge createCharge(Charge charge, String userToken) {
        String orderNo = "";
        BigDecimal amount = new BigDecimal(charge.getAmount()).divide(new BigDecimal("100"), 2, HALF_EVEN);
        if ("alipay".equals(charge.getChannel()))
            orderNo = payApply(userToken, amount, PAY_BY_ALIPAY);
        else if ("wx".equals(charge.getChannel()))
            orderNo = payApply(userToken, amount, PAY_BY_WX);
        else if ("wx_pub".equals(charge.getChannel())) {
            orderNo = payApply(userToken, amount, PAY_BY_WX_PUB);
        }else if ("alipay_pc_direct".equals(charge.getChannel())){
            orderNo = payApply(userToken,amount,ALIPAY_PC_DIRECT);
        }
        charge.setOrderNo(orderNo);
        return chargeService.createCharge(charge);
    }

    /**
     * 创建充值对象_具体
     */
    private String payApply(String userToken, BigDecimal amount, PayType payType) {
        TpAccountApplyRecord applyRecord = new TpAccountApplyRecord();
        Member member = memberRepository.findByToken(userToken);
        Account account = accountRepository.findByMemberId(member.getId());
        ifNullThrow(account, TIP_MEMBER_ACCOUNT_NOT_EXIST);
        applyRecord.setMember(member);
        applyRecord.setCreateDate(new Date());
        applyRecord.setAmount(amount);
        applyRecord.setAccount(account);
        applyRecord.setState(APPROVAL);
        applyRecord.setRecordType(BILL_RECHARGE);
        applyRecord.setPayType(payType);
        applyRecord.setOrderNo(genSerialNo());
        TpAccountApplyRecord accountPayApplyRecord = accountApplyRecordRepository.save(applyRecord);

        return accountPayApplyRecord.getOrderNo();
    }

    /**
     * 回调
     */
    public void callBack(HttpServletRequest request, HttpServletResponse response) {
        Pair<String, Event> result = verifyRequest(request);
        if (CHARGE_SUCCEEDED.equals(result.getKey())) {
            Charge charge = (Charge) result.getValue().getData().getObject();
            completeCallBack(charge.getOrderNo(), this::processChargeCalBack);
            response.setStatus(OK.value());
        } else if (REFUND_SUCCEEDED.equals(result.getKey())) {
            Transfer transfer = (Transfer) result.getValue().getData().getObject();
            completeCallBack(transfer.getOrderNo(), this::processRefundCallBack);
            response.setStatus(OK.value());
        } else {
            response.setStatus(INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * 退款回调
     * TODO 这里是退款回调逻辑
     */
    private void processRefundCallBack(TpAccountApplyRecord record, AccountFlow flow) {
        Account account = record.getAccount();
        account.setBalance(account.getBalance().subtract(record.getAmount()));
    }

    /**
     * 充值回调
     * TODO 这里是充值回调逻辑
     */
    private void processChargeCalBack(TpAccountApplyRecord record, AccountFlow flow) {
        Account account = record.getAccount();
        account.setBalance(account.getBalance().add(record.getAmount()));
    }

    /**
     * 完成回调
     */
    private void completeCallBack(String orderNo, BiConsumer<TpAccountApplyRecord, AccountFlow> consumer) {
        TpAccountApplyRecord record = accountApplyRecordRepository.findByOrderNo(orderNo);
        if (isNull(record))
            return;
        if (record.getState() == APPROVAL) {
            Account account = record.getAccount();
            record.setState(SUCCESS);
            record.setUpdateDate(new Date());

            AccountFlow accountFlow = new AccountFlow();
            accountFlow.setRecordType(record.getRecordType());
            accountFlow.setState(SUCCESS);
            accountFlow.setCreateDate(new Date());
            accountFlow.setType(record.getRecordType() == BILL_RECHARGE ? PAY_IN : PAY_OUT);
            accountFlow.setAccount(account);
            accountFlow.setAmount(record.getAmount());
            accountFlow.setOrderNo(genSerialNo());
            consumer.accept(record, accountFlow);

            accountRepository.save(account);
            accountApplyRecordRepository.save(record);
            accountFlowRepository.save(accountFlow);

        }
    }

}
