package com.thousandsunny.thirdparty.domain.service;

import com.pingplusplus.exception.PingppException;
import com.pingplusplus.model.Charge;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.service.model.*;
import com.thousandsunny.thirdparty.domain.repository.AccountFlowRepository;
import com.thousandsunny.thirdparty.domain.repository.AccountRepository;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import com.thousandsunny.thirdparty.model.ThirdPartyPayAccount;
import com.thousandsunny.thirdparty.pingpp.PingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static com.pingplusplus.model.Charge.create;
import static com.thousandsunny.common.RandomNumberUtil.genSerialNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.service.ModuleTips.TIP_NO_ACCOUNT;
import static com.thousandsunny.service.ModuleTips.TIP_UNSUPPORTED_PAY_TYPE;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_OUT;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.APPROVAL;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.*;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_PAY_OFFLINE;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.ENTREPRENEUR_APPLY;
import static java.net.InetAddress.getLocalHost;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by guitarist on 6/22/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Service
public class ThirdPartyPayAccountService extends BaseService<ThirdPartyPayAccount> {
    @Autowired
    private TpAccountApplyRecordService accountApplyService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountFlowRepository accountFlowRepository;
    @Autowired
    private PingConfig pingConfig;

    public Charge choosePayType(String userToken,
                                PayType payType,
                                BigDecimal money,
                                SourceType sourceType,
                                Job job,
                                JobApplyRecord jobApplyRecord,
                                EntrepreneursApply entrepreneursApply,
                                PartnerApply partnerApply,
                                CourseApply courseApply,
                                String subject,
                                String openId) {

        ifTrueThrow(payType == PAY_BY_BALANCE, TIP_UNSUPPORTED_PAY_TYPE);
        if (payType == PAY_OFFLINE) {
            Account account = accountRepository.findByMemberToken(userToken);
            ifNullThrow(account, TIP_NO_ACCOUNT);
            AccountFlow accountFlow = new AccountFlow();
            accountFlow.setAccount(account);
            accountFlow.setAmount(money);
            accountFlow.setSource(sourceType);
            accountFlow.setOrderNo(genSerialNo());
            accountFlow.setRecordType(BILL_PAY_OFFLINE);
            accountFlow.setPayType(PAY_OFFLINE);
            accountFlow.setState(APPROVAL);
            accountFlow.setType(PAY_OUT);
            accountFlow.setJob(job);
            accountFlow.setJobApplyRecord(jobApplyRecord);
            accountFlow.setEntrepreneursApply(entrepreneursApply);
            accountFlow.setPartnerApply(partnerApply);
            accountFlow.setCourseApply(courseApply);
            if (sourceType == ENTREPRENEUR_APPLY) {
                accountFlow.setRemark(sourceType.getRemark().replace("?", entrepreneursApply.getType().getTitle()));
            } else {
                accountFlow.setRemark(sourceType.getRemark());
            }

            accountFlowRepository.save(accountFlow);
            return null;
        } else {
            Charge charge = new Charge();
            charge.setSubject(subject);
            charge.setAmount(money.intValue() * 100);
            ifTrueThen(payType == PAY_BY_WX, () -> charge.setChannel("wx"));//微信
            ifTrueThen(payType == PAY_BY_ALIPAY, () -> charge.setChannel("alipay"));//支付宝
            ifTrueThen(payType == PAY_BY_WX_PUB, () -> { //微信公众号
                Map<String, Object> extra = new HashMap<String, Object>();
                extra.put("open_id", openId);
                charge.setChannel("wx_pub");
                charge.setExtra(extra);
            });
            ifTrueThen(payType == ALIPAY_PC_DIRECT, () -> {
                Map<String, Object> extra = new HashMap<String, Object>();
                extra.put("success_url", pingConfig.getSuccessUrl());
                charge.setChannel("alipay_pc_direct");
                charge.setExtra(extra);
            });//支付宝电脑网站支付
            return accountApplyService.createCharge(charge, userToken);
        }
    }

}
