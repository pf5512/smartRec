package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.DateUtil;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.JobApplyRecordService;
import com.thousandsunny.service.service.RenewalsRecordService;
import com.thousandsunny.thirdparty.breach.BreachConfig;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.service.ModuleTips.TIP_NO_RENEWAL_RECORD;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.BILL_REFUND;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.JOB_RESIGN;
import static java.sql.Date.valueOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_OFFLINE;
import static com.thousandsunny.thirdparty.ModuleKey.PayOfflineType.PAY_OFFLINE_BANK;

/**
 * Created by 13336 on 2017/1/11.
 */
@RestController
@RequestMapping(value = "/api/manager/contract", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerContractController {
    private String[] contract_page_info = {
            "id",
            "job.shop.name",
            "job.name",
            "job.recType.title",
            "job.reward",
            "renewals.worker.realName",
            "breach",
            "dealType.title"
    };

    private String[] contract_edit_detail = {
            "job.shop.name:shopName",
            "job.name:position",
            "job.recType.title:recruitType",
            "job.reward:rewardAmount",
            "renewals.worker.realName:workUser",
            "breach:breakContractAmount",
    };

    private String[] contract_detail = {
            "job.shop.name:shopName",
            "job.name:position",
            "job.recType.title:recruitType",
            "dealType.title:jobOprationType",
            "jobOperationType",
    };

    private String[] contract_shop_info = {
            "name:shopName",
            "id:shopId",
            "owner.realName:contactor",
            "owner.mobile:mobile",
            "owner.hpAccount:hpAccount",
            "ownerPosition.title:shopPosition",
            "address:shopAdress",
            "openTime"
    };

    private String[] contract_member_info = {
            "headImage.path:headImg",
            "id:vipId",
            "mobile",
            "realName:username",
            "username:nickname",
            "gender.title:gender",
            "hpAccount",
            "entrepreneurLevel.title:isEstp",
            "partnerLevel.title:isPartner",
            "birthday",
            "regTime",
            "referer"
    };

    @Autowired
    private RenewalsRecordService renewalsRecordService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private BreachConfig breachConfig;
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private AccountService accountService;

    /**
     * 9.11.1 岗位续费违约管理
     *
     * @Author xiao xue wei
     * @Date 2017/1/11
     */
    @RequestMapping(value = "/contractList", method = GET)
    public Result contractList(BackPageVo pageVo, String text, String tableType) {
        Page<RenewalsRecord> page = renewalsRecordService.findContractList(pageVo.pageRequest(), text, tableType);
        return OK(page.map(e -> {
            JSONObject jsonObject = propsFilter(e, contract_page_info);
            java.sql.Date now = valueOf(LocalDate.now());
            Date date = new Date(now.getTime());
            jsonObject.put("breakContractDays", DateUtil.dayGap(new Date(e.getFinalDate().getTime()), date));//违约天数
            jsonObject.put("joinTime", ISO_DATE_FORMAT.format(new Date(e.getRenewals().getAvaDate().getTime())));
            jsonObject.put("validTime", ISO_DATE_FORMAT.format(e.getFinalDate()));
            return jsonObject;
        }));
    }

    /**
     * 9.11.2 岗位续费违约管理---编辑详情
     *
     * @Author xiao xue wei
     * @Date 2017/1/11
     */
    @RequestMapping(value = "/editDetail", method = GET)
    public Result editDetail(Long id) {
        JSONObject jsonObject = new JSONObject();
        //岗位续费违约详情
        RenewalsRecord renewalsRecord = renewalsRecordService.findOne(id);
        JSONObject breakContractDetails = propsFilter(renewalsRecord, contract_edit_detail);
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(renewalsRecord.getRenewals().getWorker().getId(), renewalsRecord.getJob().getId());

        java.sql.Date now = valueOf(LocalDate.now());
        Date nowTime = new Date(now.getTime());
        Integer days = DateUtil.dayGap(new Date(renewalsRecord.getFinalDate().getTime()), nowTime);//违约天数
        BigDecimal breach = ((renewalsRecord.getRenewals().getFee().multiply(new BigDecimal(breachConfig.getScale())).multiply(new BigDecimal(days)))
                .setScale(0, BigDecimal.ROUND_UP)).add(new BigDecimal(breachConfig.getMinBreach()));
        breakContractDetails.put("remainAmount", jobApplyRecord.getJob().getReward().multiply(new BigDecimal(3)).subtract(breach));

        breakContractDetails.put("joinTime", ISO_DATE_FORMAT.format(renewalsRecord.getRenewals().getAvaDate()));
        breakContractDetails.put("startTime", ISO_DATE_FORMAT.format(renewalsRecord.getStartDate()));
        breakContractDetails.put("endTime", ISO_DATE_FORMAT.format(renewalsRecord.getFinalDate()));
        breakContractDetails.put("breakContractDays", days);
        jsonObject.put("breakContractDetails", breakContractDetails);
        //店铺基础信息
        Shop shop = renewalsRecord.getJob().getShop();
        JSONObject shopBaseInfo = propsFilter(shop, contract_shop_info);
        StringBuffer shopArea = new StringBuffer();
        ifNotNullThen(shop.getProvince(), t -> shopArea.append(t.getName()));
        ifNotNullThen(shop.getCity(), t -> shopArea.append("-").append(t.getName()));
        ifNotNullThen(shop.getArea(), t -> shopArea.append("-").append(t.getName()));
        shopBaseInfo.put("shopArea", shopArea);
        shopBaseInfo.put("shopXYZ", shop.getLongitude() + "," + shop.getLatitude());
        ifNotNullThen(shop.getDate(), t -> shopBaseInfo.replace("openTime", ISO_DATE_FORMAT.format(shop.getDate())));
        jsonObject.put("shopBaseInfo", shopBaseInfo);
        //个人信息
        Member member = renewalsRecord.getRenewals().getWorker();
        JSONObject userBaseInfo = propsFilter(member, contract_member_info);
        ifNotNullThen(member.getBirthday(), x -> userBaseInfo.replace("birthday", ISO_DATE_FORMAT.format(x)));
        ifNotNullThen(member.getCreateTime(), x -> userBaseInfo.replace("regTime", ISO_DATE_FORMAT.format(x)));
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(member.getToken());
        if (isNotNull(memberExtInfo)) {
            Member recommendUser = memberExtInfo.getRecommendUser();
            if (isNotNull(recommendUser)) userBaseInfo.replace("referer", recommendUser.getRealName());
        }
        jsonObject.put("userBaseInfo", userBaseInfo);

        return OK(jsonObject);
    }

    /**
     * 9.11.3 岗位续费违约管理---编辑
     *
     * @Author xiao xue wei
     * @Date 2017/1/12
     */
    @RequestMapping(value = "/edit", method = POST)
    public Result edit(Long id, Date leaveTime, String remark) {
        RenewalsRecord renewalsRecord = renewalsRecordService.findOne(id);
        ifNullThrow(renewalsRecord, TIP_NO_RENEWAL_RECORD);
        Map<String, Object> map = computeInfoMap(id, leaveTime);
        //1. 平台让员工离职
        jobApplyRecordService.leaveWorkByPlatform(renewalsRecord, leaveTime);
        //2. 续费记录修改
        renewalsRecordService.editRenewalsRecord(renewalsRecord.getId(), (BigDecimal) (map.get("amount")), remark);
        //2.1 自动续费设置数据伪删除(isDelete = YES)
        renewalsRecordService.deleteAutoRenewals(renewalsRecord.getRenewals());
        //3. 给店铺退押金
        Account shopAccount = accountService.findByMemberToken(renewalsRecord.getJob().getShop().getOwner().getToken());
        BigDecimal remainAmount = (BigDecimal) (map.get("remainAmount"));
        accountService.zuesAccountRefundMoney(shopAccount, remainAmount);
        //3.1 平台扣钱流水
        Account platformAccount = accountService.findZuesAccount();
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(renewalsRecord.getRenewals().getWorker().getId(), renewalsRecord.getJob().getId());
        accountFlowService.savePlatformFlow(jobApplyRecord, jobApplyRecord.getJob(), platformAccount, remainAmount, JOB_RESIGN, BILL_REFUND, null, null, null);
        //3.2店家账户加钱流水
        accountFlowService.saveShopOwnerFlow(jobApplyRecord, shopAccount, remainAmount);
        //4. 平台产生收益(岗位续费违约收益)
        BigDecimal breach = (BigDecimal) (map.get("amount"));
        accountFlowService.savePlatformEarningsFlow(jobApplyRecord, platformAccount, breach);
        return OK();
    }

    /**
     * 9.11.4 详情
     *
     * @Author xiao xue wei
     * @Date 2017/2/23
     */
    @RequestMapping(value = "/detail", method = GET)
    public Result detail(Long id) {
        //岗位续费违约详情
        RenewalsRecord renewalsRecord = renewalsRecordService.findOne(id);
        JSONObject breakContractDetails = propsFilter(renewalsRecord, contract_detail);
        enumToJson(renewalsRecord.getDealType(), breakContractDetails, "jobOperationType");
        JobApplyRecord jobApplyRecord = jobApplyRecordService.
                findByReceiverTokenAndJobId(renewalsRecord.getRenewals().getWorker().getId(), renewalsRecord.getJob().getId());
        if (renewalsRecord.getDealType() == ModuleKey.RenewalsDealType.DEAL_BY_PLATFORM) {//平台接入处理
            parseDealByPlatFormBreach(breakContractDetails, renewalsRecord, jobApplyRecord);
        } else if (renewalsRecord.getDealType() == ModuleKey.RenewalsDealType.DEAL_BY_RENEWAL) {//续费
            parseDealByRenewalBreach(breakContractDetails, renewalsRecord, jobApplyRecord);
        } else if (renewalsRecord.getDealType() == ModuleKey.RenewalsDealType.DEAL_BY_RETURN) {//退押金
            parseDealByReturnBreach(breakContractDetails, renewalsRecord, jobApplyRecord);
        }
        return OK(breakContractDetails);
    }

    private void parseDealByReturnBreach(JSONObject breakContractDetails, RenewalsRecord renewalsRecord, JobApplyRecord jobApplyRecord) {
        HpApply hpApply = jobApplyRecord.getHpApply();
        if (isNotNull(hpApply)) {
            breakContractDetails.put("workNumber", hpApply.getRefundCount());
            breakContractDetails.put("totalAmount", hpApply.getMoney());
            breakContractDetails.put("refundWay", "账户余额");
            breakContractDetails.put("operation", null);
            breakContractDetails.put("remark", null);
            if (isNotNull(hpApply.getDate()))
                breakContractDetails.put("applyRefundTime", ISO_DATE_FORMAT.format(hpApply.getDate()));
            else breakContractDetails.put("applyRefundTime", null);
            ifNotNullThen(hpApply.getState(), e -> enumToJson(e, breakContractDetails, "operation"));
            ifNotBlankThen(hpApply.getRemark(), e -> breakContractDetails.replace("remark", e));
            AccountFlow accountFlow = accountFlowService.findReturnDepositHpApplyFlow(hpApply);
            if (isNotNull(accountFlow)) {
                if (isNotNull(accountFlow.getCreateDate()))
                    breakContractDetails.put("refundTime", ISO_DATETIME_FORMAT.format(hpApply.getDate()));
                else breakContractDetails.put("refundTime", null);
                if (isNotNull(accountFlow.getCreateDate()))
                    breakContractDetails.put("refundAuditTime", ISO_DATETIME_FORMAT.format(hpApply.getDate()));
                else breakContractDetails.put("refundAuditTime", null);
                breakContractDetails.put("refundFlow", accountFlow.getOrderNo());
            }
        }
    }

    private void parseDealByRenewalBreach(JSONObject breakContractDetails, RenewalsRecord renewalsRecord, JobApplyRecord jobApplyRecord) {
        breakContractDetails.put("workNumber", 1);
        breakContractDetails.put("totalAmount", jobApplyRecord.getJob().getReward().add(renewalsRecord.getBreach()));
        breakContractDetails.put("refundDays", DateUtil.dayGap(new Date(renewalsRecord.getFinalDate().getTime()), renewalsRecord.getDate()));
        breakContractDetails.put("refund", renewalsRecord.getRenewals());
        if (isNotNull(renewalsRecord.getRenewals().getWorker().getRealName()))
            breakContractDetails.put("workStuff", renewalsRecord.getRenewals().getWorker().getRealName());
        else breakContractDetails.put("workStuff", renewalsRecord.getRenewals().getWorker().getMobile());
        if (isNotNull(renewalsRecord.getRenewals().getAvaDate()))
            breakContractDetails.put("joinTime", ISO_DATE_FORMAT.format(renewalsRecord.getRenewals().getAvaDate()));
        else breakContractDetails.put("joinTime", null);

        AccountFlow accountFlow = renewalsRecord.getAccountFlow();
        if (isNotNull(accountFlow)) {
            breakContractDetails.put("payWay", null);
            ifNotNullThen(accountFlow.getPayType(), e -> enumToJson(e, breakContractDetails, "payWay"));
            breakContractDetails.put("payStatus", null);
            ifNotNullThen(accountFlow.getState(), e -> enumToJson(e, breakContractDetails, "payStatus"));
            if (isNotNull(accountFlow.getCreateDate())) {
                breakContractDetails.put("payApplyTime", ISO_DATE_FORMAT.format(accountFlow.getCreateDate()));
                breakContractDetails.put("payTime", ISO_DATE_FORMAT.format(accountFlow.getCreateDate()));
            } else {
                breakContractDetails.put("payApplyTime", null);
                breakContractDetails.put("payTime", null);
            }
            breakContractDetails.put("payFlow", accountFlow.getOrderNo());
            breakContractDetails.put("offlinePayWay", accountFlow.getPayOfflineType().getTitle());
            if (accountFlow.getPayType() == PAY_OFFLINE && accountFlow.getPayOfflineType() == PAY_OFFLINE_BANK) {
                if (isNotBlank(accountFlow.getRemarks())) {
                    JSONObject remarks = JSON.parseObject(accountFlow.getRemarks());
                    ifNotBlankThen((String) remarks.get("payBank"), e -> breakContractDetails.put("bankName", e));
                    ifNotBlankThen((String) remarks.get("bankFlow"), e -> breakContractDetails.put("bankFlow", e));
                    ifNotBlankThen((String) remarks.get("content"), e -> breakContractDetails.put("remark", e));
                }
            }
        }
    }

    private void parseDealByPlatFormBreach(JSONObject breakContractDetails, RenewalsRecord renewalsRecord, JobApplyRecord jobApplyRecord) {
        if (isNotNull(renewalsRecord.getRenewals().getAvaDate()))
            breakContractDetails.put("joinTime", ISO_DATE_FORMAT.format(renewalsRecord.getRenewals().getAvaDate()));
        else breakContractDetails.put("joinTime", null);
        if (isNotNull(renewalsRecord.getStartDate()))
            breakContractDetails.put("renewStartTime", ISO_DATE_FORMAT.format(renewalsRecord.getStartDate()));
        else breakContractDetails.put("renewStartTime", null);
        if (isNotNull(renewalsRecord.getFinalDate()))
            breakContractDetails.put("renewSustainTime", ISO_DATE_FORMAT.format(renewalsRecord.getFinalDate()));
        else breakContractDetails.put("renewSustainTime", null);
        if (isNotNull(jobApplyRecord.getResignDate()))
            breakContractDetails.put("negotiateTime", ISO_DATE_FORMAT.format(jobApplyRecord.getResignDate()));
        else breakContractDetails.put("negotiateTime", null);
        if (isNotNull(renewalsRecord.getRenewals().getWorker().getRealName()))
            breakContractDetails.put("workStuff", renewalsRecord.getRenewals().getWorker().getRealName());
        else breakContractDetails.put("workStuff", renewalsRecord.getRenewals().getWorker().getMobile());
        breakContractDetails.put("deposit", jobApplyRecord.getJob().getReward().multiply(new BigDecimal(3)).subtract(renewalsRecord.getBreach()));
        breakContractDetails.put("refund", renewalsRecord.getBreach());
        breakContractDetails.put("refundDays", DateUtil.dayGap(new Date(renewalsRecord.getFinalDate().getTime()), jobApplyRecord.getResignDate()));
    }

    /**
     * 9.11.5 岗位续费违约管理---计算违约天数、违约金、剩余押金
     *
     * @Author xiao xue wei
     * @Date 2017/1/12
     */
    @RequestMapping(value = "/computeInfo", method = GET)
    public Result computeInfo(Long id, Date leaveTime) {
        JSONObject jsonObject = new JSONObject();
        Map<String, Object> map = computeInfoMap(id, leaveTime);
        jsonObject.put("days", map.get("days"));
        jsonObject.put("amount", map.get("amount"));
        jsonObject.put("remainAmount", map.get("remainAmount"));
        return OK(jsonObject);
    }

    /**
     * 计算违约天数、违约金、剩余押金
     */
    public Map<String, Object> computeInfoMap(Long id, Date leaveTime) {
        Map<String, Object> map = new HashMap<>();
        RenewalsRecord renewalsRecord = renewalsRecordService.findOne(id);
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findByReceiverTokenAndJobId(renewalsRecord.getRenewals().getWorker().getId(), renewalsRecord.getJob().getId());
        Integer days = DateUtil.dayGap(new Date(renewalsRecord.getFinalDate().getTime()), leaveTime);//违约天数
        BigDecimal breach = ((renewalsRecord.getRenewals().getFee().multiply(new BigDecimal(breachConfig.getScale())).multiply(new BigDecimal(days)))
                .setScale(0, BigDecimal.ROUND_UP)).add(new BigDecimal(breachConfig.getMinBreach()));
        map.put("days", days);
        map.put("amount", breach);
        map.put("remainAmount", jobApplyRecord.getJob().getReward().multiply(new BigDecimal(3)).subtract(breach));
        return map;
    }

}
