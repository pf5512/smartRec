package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.service.model.PartnerApply;
import com.thousandsunny.service.model.RenewalsRecord;
import com.thousandsunny.service.model.WithdrawAccount;
import com.thousandsunny.service.service.WithdrawAccountService;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleTips.TIP_NO_ACCOUNT;
import static com.thousandsunny.service.ModuleTips.TIP_NO_RENEWAL_RECORD;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_REFUND;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_WITHDRAW;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.PARTNER_APPLY;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/portal/withdrawAccount", produces = APPLICATION_JSON_UTF8_VALUE)
public class WithdrawAccountController {

    private static final String[] withdraw_account_list_json = {
            "id",
            "type",
            "name:accountName",
            "account:accountNumber",
            "bank:bankName",
            "branchBank:branchBankName",
    };

    private static final String[] withdraw_account_detail_json = {
            "id",
            "type",
            "name:accountName",
            "account:accountNumber",
            "bank:bankName",
            "branchBank:branchBankName",
            "idCard.path:idCardFrontImageUrl",
            "half.path:halfImageUrl"
    };

    private static final String[] account_flow_list_json = {
            "id",
            "recordType:type",
            "type:ieType",
            "createDate:date",
            "remark",
            "state",
            "source:incomeType",
            "amount",
            "account.balance:balance"
    };

    private static final String[] account_flow_detail_json = {
            "id",
            "amount",
            "recordType:type",
            "type:ieType",
            "payType",
            "createDate:date",
            "remark",
            "applyDate",
            "receivedDate",
            "reason:failedReason",
            "state",
            "source:incomeType",
            "withdrawAccount",
            "partnerArea"
    };
    private static final String[] renewals_record_json = {
            "id",
            "accountFlow.recordType:type",
            "accountFlow.payType:payType",
            "date",
            "accountFlow.remark:remark",
            "accountFlow.amount:amount"
    };
    @Autowired
    private WithdrawAccountService withdrawAccountService;
    @Autowired
    private AccountService accountService;

    /**
     * 提现账户详情
     */
    @RequestMapping(value = "/{id}", method = GET)
    public ResponseEntity get(String userToken, @PathVariable Long id) {
        WithdrawAccount withdrawAccount = withdrawAccountService.findByIdAndMemberToken(id, userToken);
        return ok(propsFilter(withdrawAccount, withdraw_account_detail_json));
    }

    /**
     * 提现账户列表
     */
    @RequestMapping(value = "/list", method = GET)
    public ResponseEntity list(String userToken) {
        List<WithdrawAccount> withdrawAccounts = withdrawAccountService.getWithdrawAccounts(userToken);
        List<JSONObject> jsonObjects = simpleMap(withdrawAccounts, e -> propsFilter(e, withdraw_account_list_json));
        return ok(listToJson(jsonObjects));
    }

    /**
     * 新增/编辑提现账户
     */
    @RequestMapping(method = POST)
    public ResponseEntity edit(String userToken, WithdrawAccount withdrawAccount) {
        withdrawAccountService.edit(userToken, withdrawAccount);
        return OK;
    }

    /**
     * 删除提现账户
     */
    @RequestMapping(value = "/delete", method = PUT)
    public ResponseEntity delete(String userToken, Long id) {
        withdrawAccountService.delete(userToken, id);
        return OK;
    }

    /**
     * 账单流水列表
     */
    @RequestMapping(value = "/flows", method = GET)
    public ResponseEntity flows(String userToken, PageVO pageVO) {
        Page<AccountFlow> accountFlows = withdrawAccountService.listFlows(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(accountFlows, e -> {
            JSONObject object = propsFilter(e, account_flow_list_json);
            ifFalseThen(e.getRecordType() == BILL_WITHDRAW || e.getRecordType() == BILL_REFUND, () -> object.put("state", null));
            return object;
        });
        return ok(jsonObject);
    }

    /**
     * 账单流水详情
     */
    @RequestMapping(value = "/details", method = GET)
    public ResponseEntity showFlowDetails(String userToken, Long id) {
        AccountFlow accountFlow = withdrawAccountService.showFlowDetails(userToken, id);
        JSONObject jsonObject = propsFilter(accountFlow, account_flow_detail_json);
        ifNotNullThen(accountFlow.getWithdrawAccount(), a -> jsonObject.put("withdrawAccount", a.getType()));

        PartnerApply partnerApply = accountFlow.getPartnerApply();
        ifTrueThen(accountFlow.getSource() == PARTNER_APPLY && isNotNull(partnerApply),
                () -> jsonObject.put("partnerArea", wrapString(partnerApply)));
        return ok(jsonObject);
    }

    private String wrapString(PartnerApply apply) {
        StringBuilder builder = new StringBuilder();
        ifNotBlankThen(apply.getProvinceName(), builder::append);
        ifNotBlankThen(apply.getCityName(), c -> builder.append("-").append(c));
        ifNotBlankThen(apply.getAreaName(), a -> builder.append("-").append(a));

        ifNotBlankThen(apply.getProvince2Name(), p -> builder.append(",").append(p));
        ifNotBlankThen(apply.getCity2Name(), c -> builder.append("-").append(c));
        ifNotBlankThen(apply.getArea2Name(), a -> builder.append("-").append(a));

        return builder.toString();
    }

    /**
     * 岗位推荐在职员工按月悬赏往期续费记录
     */
    @RequestMapping(value = "/records", method = GET)
    public ResponseEntity showRecords(String userToken, Long jobId, PageVO pageVO) {
        Page<RenewalsRecord> renewalsRecords = withdrawAccountService.showRecords(userToken, jobId, pageVO.pageRequest());
        ifEmptyThrow(renewalsRecords.getContent(), TIP_NO_RENEWAL_RECORD);

        return ok(pageToJson(renewalsRecords, e -> propsFilter(e, renewals_record_json)));
    }

    /**
     * 发起提现申请
     */
    @RequestMapping(value = "/withdrawApply", method = POST)
    public ResponseEntity apply(String userToken, BigDecimal amount, String payPassword, Long withdrawAccountId, Integer code) {
        withdrawAccountService.withdrawApply(userToken, amount, payPassword, withdrawAccountId, code);
        return OK;
    }

    /**
     * 17.5 账单流水详情
     *
     * @Author xiao xue wei
     * @Date 2017/3/15
     */
    @RequestMapping(value = "/cashAmount", method = GET)
    public ResponseEntity cashAmount(String userToken) {
        Account account = accountService.findByMemberToken(userToken);
        ifNullThrow(account, TIP_NO_ACCOUNT);
        JSONObject jsonObject = new JSONObject();
        Double amount = account.getBalance().subtract(account.getFreezingAmount()).doubleValue();
        jsonObject.put("amount", amount > 5000 ? 5000 : amount);
        return ok(jsonObject);
    }
}
