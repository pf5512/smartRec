package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey.EntrepreneursType;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.SchoolRepository;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.MemberService;
import com.thousandsunny.service.ModuleKey.RecruitmentType;
import com.thousandsunny.thirdparty.ModuleKey.FlowState;
import com.thousandsunny.thirdparty.ModuleKey.PayOfflineType;
import com.thousandsunny.thirdparty.ModuleKey.PayType;
import com.thousandsunny.thirdparty.ModuleKey.SourceType;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.*;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.service.ModuleKey.EntrepreneursType.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.ONCE;
import static com.thousandsunny.service.ModuleKey.WithdrawType;
import static com.thousandsunny.service.ModuleKey.WithdrawType.WITHDRAW_ACCOUNT_BANK_CARD;
import static com.thousandsunny.thirdparty.ModuleKey.ChargeType.PAY_IN;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.APPROVAL;
import static com.thousandsunny.thirdparty.ModuleKey.FlowState.SUCCESS;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_OFFLINE;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.PLATFORM_IN;
import static com.thousandsunny.thirdparty.ModuleKey.RecordType.PLATFORM_INCOME;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.*;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2016/12/1.
 */
@RestController
@RequestMapping(value = "/api/manager/accountFlow", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerAccountFlowController {

    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private AccountService accountService;

    private static final String[] WITHDRAW_LIST_JSON = new String[]{
            "id",
            "account.member.realName",
            "withdrawAccount.account",
            "withdrawAccount.type.title",
            "account.member.role",
            "amount",
            "createDate",
            "withdrawAccount.type",
            "payStatus"
    };
    private static final String[] PLATFORM_REVENUE_ACCOUNTFLOW_JSON = new String[]{
            "id",
            "amount",
            "source.title:type",
            "source",
            "dateTime"
    };

    private static final String[] REVENUE_SOURCE_JSON = new String[]{
            "account.member.realName:username",
            "account.member.mobile:mobile",
            "account.member.entrepreneurLevel:estpType",
            "referer",
            "fee",
            "payType:payWay",
            "state.title:payStatus",
            "orderNo:payFlow",
            "payTime",
            "payUser",
            "payAccount"
    };


    /**
     * 9.1.1 平台收支统计
     *
     * @Author mu.jie
     * @Date 2016/12/1
     */
    @RequestMapping(value = "/platformCount", method = GET)
    public Result countPlatform(Date startTime, Date endTime) {
        JSONObject body = new JSONObject();
        JSONObject income = new JSONObject();//收入
        BigDecimal entrepreneurIncome = accountFlowService.totalPlatformIn(startTime, endTime, PLATFORM_IN, ENTREPRENEUR_APPLY, null);
        BigDecimal entrepreneurIncomeB = isNull(entrepreneurIncome) ? ZERO : entrepreneurIncome;
        income.put("cyzIncome", entrepreneurIncomeB);
        BigDecimal partnerIncome = accountFlowService.totalPlatformIn(startTime, endTime, PLATFORM_IN, PARTNER_APPLY, null);
        BigDecimal partnerIncomeB = isNull(partnerIncome) ? ZERO : partnerIncome;
        income.put("hhrIncome", partnerIncomeB);
        BigDecimal jobIncome = accountFlowService.totalPlatformIn(startTime, endTime, PLATFORM_IN, null, newArrayList(JOB_NEW, JOB_ADD));
        BigDecimal jobIncomeB = isNull(jobIncome) ? ZERO : jobIncome;
        income.put("recruitIncome", jobIncomeB);
        BigDecimal courseIncomeB = ZERO;//TODO:mu.jie 课程报名收入
        income.put("courseIncome", courseIncomeB);
        income.put("allIncome", entrepreneurIncomeB.add(partnerIncomeB).add(jobIncomeB).add(courseIncomeB));

        JSONObject earnings = new JSONObject();//收益
        BigDecimal entrepreneurIn = accountFlowService.totalPlatformIn(startTime, endTime, PLATFORM_INCOME, ENTREPRENEUR_AWARD, null);
        BigDecimal entrepreneurInB = isNull(entrepreneurIn) ? ZERO : entrepreneurIn;
        earnings.put("cyzEarnings", entrepreneurInB);
        BigDecimal partnerIn = accountFlowService.totalPlatformIn(startTime, endTime, PLATFORM_IN, PARTNER_APPLY, null);
        BigDecimal partnerInB = isNull(partnerIn) ? ZERO : partnerIn;
        earnings.put("hhrEarnings", partnerInB);
        BigDecimal jobIn = accountFlowService.totalPlatformIn(startTime, endTime, PLATFORM_INCOME, PLATFORM_PROFIT_JOB, null);
        BigDecimal jobInB = isNull(jobIn) ? ZERO : jobIn;
        earnings.put("recruitEarnings", jobInB);
        BigDecimal courseInB = ZERO;//TODO:mu.jie 课程报名收益
        earnings.put("courseEarnings", courseInB);
        BigDecimal deditIn = accountFlowService.totalPlatformIn(startTime, endTime, PLATFORM_INCOME, DEFAULTS, null);
        BigDecimal deditInB = isNull(deditIn) ? ZERO : deditIn;
        earnings.put("deditEarnings", deditInB);
        earnings.put("allEarnings", entrepreneurInB.add(partnerInB).add(jobInB).add(courseInB).add(deditInB));

        JSONObject disburse = new JSONObject();
        BigDecimal accountBalance = accountFlowService.totalMemberAccount(startTime, endTime, null);
        BigDecimal accountBalanceB = isNull(accountBalance) ? ZERO : accountBalance;
        disburse.put("accountBalance", accountBalanceB);
        BigDecimal accountBalanceUn = accountFlowService.totalMemberAccountUnWithdraw(startTime, endTime, null);
        BigDecimal accountBalanceUnB = isNull(accountBalanceUn) ? ZERO : accountBalanceUn;
        disburse.put("accountBalanceUn", accountBalanceUnB);
        BigDecimal tixianBalance = accountFlowService.totalBillWithdraw(startTime, endTime);
        BigDecimal tixianBalanceB = isNull(tixianBalance) ? ZERO : tixianBalance;
        disburse.put("tixianBalance", tixianBalanceB);
        BigDecimal schoolBalance = accountFlowService.totalMemberAccount(startTime, endTime, "SCHOOL");
        BigDecimal schoolBalanceB = isNull(schoolBalance) ? ZERO : schoolBalance;
        disburse.put("schoolBalance", schoolBalanceB);
        BigDecimal schoolBalanceUn = accountFlowService.totalMemberAccountUnWithdraw(startTime, endTime, "SCHOOL");
        BigDecimal schoolBalanceUnB = isNull(schoolBalanceUn) ? ZERO : schoolBalanceUn;
        disburse.put("schoolBalanceUn", schoolBalanceUnB);
        BigDecimal schoolTixianBalance = accountFlowService.totalBillWithdrawOfSchool(startTime, endTime);
        BigDecimal schoolTixianBalanceB = isNull(schoolTixianBalance) ? ZERO : schoolTixianBalance;
        disburse.put("schoolTixianBalance", schoolTixianBalanceB);
        disburse.put("allBalance", accountBalanceB.add(tixianBalanceB).add(schoolBalanceB)
                .add(schoolTixianBalanceB).add(accountBalanceUnB).add(schoolBalanceUnB));

        JSONObject award = new JSONObject();
        BigDecimal oneTimeAward = accountFlowService.totalJobApplyRecordOfOnce(startTime, endTime, ONCE);
        BigDecimal oneTimeAwardB = isNull(oneTimeAward) ? ZERO : oneTimeAward;
        award.put("oneTimeAward", oneTimeAwardB);
        BigDecimal monthlyAward = accountFlowService.totalJobApplyRecordOfMonthly(startTime, endTime, MONTHLY);
        BigDecimal monthlyAwardB = isNull(monthlyAward) ? ZERO : monthlyAward;
        award.put("mounthTimeAward", monthlyAwardB);
        BigDecimal mounthlyDeposit = accountFlowService.totalJobApplyRecordOfMonthlyDeposit(startTime, endTime);
        BigDecimal mounthlyDepositB = isNull(mounthlyDeposit) ? ZERO : mounthlyDeposit;
        award.put("mounthTimeDeposit", mounthlyDepositB);
        award.put("allaWard", oneTimeAwardB.add(mounthlyDepositB).add(monthlyAwardB));

        body.put("income", income);
        body.put("earnings", earnings);
        body.put("disburse", disburse);
        body.put("awrd", award);
        return OK(body);
    }

    /**
     * 9.2.1 创业者付款流水列表
     *
     * @Author mu.jie
     * @Date 2016/12/1
     */
    @RequestMapping(value = "/entrepreneur", method = GET)
    public Result entrepreneurList(BackPageVo backPageVo, String text, FlowState tableType, Date startTime, Date endTime, EntrepreneursType ESTPType, PayType payWay) {
        Page<AccountFlow> page = accountFlowService.findAccountFlowList(backPageVo, decodePathVariable(text), tableType, startTime, endTime, ESTPType, payWay, "entrepreneurs", null, null, null);
        String[] json = {"id", "entrepreneursApply.member.realName", "entrepreneursApply.member.hpAccount", "regUser",
                "entrepreneursApply.type.title", "updateDate", "payType", "state", "orderNo"};
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, json);
            MemberExtInfo memberExtInfo = null;
            if (x.getEntrepreneursApply() != null && x.getEntrepreneursApply().getMember() != null) {
                memberExtInfo = memberExtInfoService.findByMemberToken(x.getEntrepreneursApply().getMember().getToken());
            }
            final MemberExtInfo finalMemberExtInfo = memberExtInfo;
            ifTrueThen(memberExtInfo != null && memberExtInfo.getRecommendUser() != null, () -> jo.replace("regUser", finalMemberExtInfo.getRecommendUser().getRealName()));
            ifNotNullThen(x.getUpdateDate(), t -> jo.replace("updateDate", ISO_DATETIME_FORMAT.format(t)));
            enumToJson(x.getPayType(), jo, "payType");
            parseFlowState(x.getState(), jo, "state");
            return jo;
        }));
    }

    /**
     * 9.2.2 已付款统计
     *
     * @Author mu.jie
     * @Date 2016/12/2
     */
    @RequestMapping(value = "/entrepreneurCount", method = GET)
    public Result entrepreneurCount(String text, Date startTime, Date endTime, EntrepreneursType ESTPType, PayType payWay) {
        BigDecimal cjB = accountFlowService.countJoinMoney(FlowState.SUCCESS, APPLY_JUNIOR, decodePathVariable(text), startTime, endTime, ESTPType, payWay);
        BigDecimal cj = cjB != null ? cjB : ZERO;
        BigDecimal gjB = accountFlowService.countJoinMoney(FlowState.SUCCESS, APPLY_SENIOR, decodePathVariable(text), startTime, endTime, ESTPType, payWay);
        BigDecimal gj = gjB != null ? gjB : ZERO;
        BigDecimal csgB = accountFlowService.countJoinMoney(FlowState.SUCCESS, APPLY_JUNIOR_TO_SENIOR, decodePathVariable(text), startTime, endTime, ESTPType, payWay);
        BigDecimal csg = csgB != null ? csgB : ZERO;
        BigDecimal total = cj.add(gj).add(csg);
        JSONObject body = new JSONObject();
        body.put("cj", cj);
        body.put("gj", gj);
        body.put("csg", csg);
        body.put("total", total);
        return OK(body);
    }

    /**
     * 9.2.3 编辑
     *
     * @Author mu.jie
     * @Date 2016/12/2
     */
    @RequestMapping(value = "/entrepreneur", method = POST)
    public Result updateEntreperneur(Long id, FlowState state, Date date, String username, PayOfflineType payWay, String payBank, String bankNO, String bankFlow, String receiveBank, String content) {
        accountFlowService.updateAccountFlow(id, state, date, username, payWay, payBank, bankNO, bankFlow, receiveBank, content);
        return OK("success");
    }

    /**
     * 9.2.4 详情
     *
     * @Author mu.jie
     * @Date 2016/12/2
     */
    @RequestMapping(value = "/entrepreneurInfo", method = GET)
    public Result entrepreneurInfo(Long id) {
        AccountFlow one = accountFlowService.findOne(id);
        String[] JSON = {"entrepreneursApply.member.realName:username", "entrepreneursApply.member.mobile:mobile", "entrepreneursApply.type.title:ESTPtype",
                "regUser", "entrepreneursApply.joinMoney:fee", "createDate:date", "updateDate:payDate", "state.title:payStatus", "orderNo:payFlow", "name", "payWay",
                "payOfflineType.title:payType", "payBank", "bankNO", "bankFlow", "receiveBank", "content", "thirdPartyPayAccount.accountNo:payAccount"};
        JSONObject body = propsFilter(one, JSON);
        ifNotNullThen(one.getUpdateDate(), x -> body.replace("payDate", ISO_DATETIME_FORMAT.format(x)));
        ifNotNullThen(one.getCreateDate(), x -> body.replace("date", ISO_DATETIME_FORMAT.format(x)));
        ifNotNullThen(one.getPayType(), x -> enumToJson(x, body, "payWay"));
        if (one.getEntrepreneursApply() != null && one.getEntrepreneursApply().getMember() != null) {
            MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(one.getEntrepreneursApply().getMember().getToken());
            ifTrueThen(memberExtInfo != null && memberExtInfo.getRecommendUser() != null, () -> body.replace("regUser", memberExtInfo.getRecommendUser().getRealName()));
        }
        parseRemark(one, body);
        return OK(body);
    }

    /**
     * 9.4.1 合伙人付款流水列表
     *
     * @Author mu.jie
     * @Date 2016/12/2
     */
    @RequestMapping(value = "/partner", method = GET)
    public Result findPartnerList(BackPageVo backPageVo, String text, FlowState tableType, Date startTime, Date endTime, PayType payWay) {
        String[] JSON = {"id", "partnerApply.member.realName", "partnerApply.member.hpAccount", "area", "updateDate", "payType", "state", "orderNo"};
        Page<AccountFlow> page = accountFlowService.findAccountFlowList(backPageVo, decodePathVariable(text), tableType, startTime, endTime, null, payWay, "partner", null, null, null);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, JSON);
            ifNotNullThen(x.getCreateDate(), t -> jo.replace("updateDate", ISO_DATETIME_FORMAT.format(t)));
            parseFlowState(x.getState(), jo, "state");
            enumToJson(x.getPayType(), jo, "payType");
            StringBuffer sb = new StringBuffer();
            ifNotNullThen(x.getPartnerApply(), a -> {
                ifNotNullThen(a.getProvince(), t -> sb.append(t.getName()));
                ifNotNullThen(a.getCity(), t -> sb.append("-").append(t.getName()));
                ifNotNullThen(a.getArea(), t -> sb.append("-").append(t.getName()));
                ifNotNullThen(a.getProvince2(), t -> sb.append("、").append(t.getName()));
                ifNotNullThen(a.getCity2(), t -> sb.append("-").append(t.getName()));
                ifNotNullThen(a.getArea2(), t -> sb.append("-").append(t.getName()));
            });
            jo.replace("area", sb.toString());
            return jo;
        }));
    }

    /**
     * 9.4.2 已付款统计
     *
     * @Author mu.jie
     * @Date 2016/12/3
     */
    @RequestMapping(value = "/partnerCount", method = GET)
    public Result countPartner(String text, Date startTime, Date endTime, PayType payWay) {
        BigDecimal bigDecimal1 = accountFlowService.countPartentJoinMoney(FlowState.SUCCESS, decodePathVariable(text), startTime, endTime, payWay);
        BigDecimal bigDecimal = bigDecimal1 != null ? bigDecimal1 : ZERO;
        JSONObject body = new JSONObject();
        body.put("total", bigDecimal);
        return OK(body);
    }

    /**
     * 9.4.3 编辑
     *
     * @Author mu.jie
     * @Date 2016/12/3
     */
    @RequestMapping(value = "/partner", method = POST)
    public Result updatePartner(Long id, FlowState state, Date date, String username, PayOfflineType payWay, String payBank, String bankNO, String bankFlow, String receiveBank, String content) {
        accountFlowService.updateAccountFlow(id, state, date, username, payWay, payBank, bankNO, bankFlow, receiveBank, content);
        return OK("success");
    }

    /**
     * 9.4.4 详情
     *
     * @Author mu.jie
     * @Date 2016/12/3
     */
    @RequestMapping(value = "/partnerInfo", method = GET)
    public Result partnerInfo(Long id) {
        AccountFlow one = accountFlowService.findOne(id);
        String[] JSON = {"partnerApply.member.realName:username", "partnerApply.member.mobile:mobile", "area", "partnerApply.joinMoney:fee", "updateDate:payDate", "payType:payWay", "createDate",
                "state.title:payStatus", "orderNo:payFlow", "name", "payOfflineType.title:payType", "payBank", "bankNO", "bankFlow", "receiveBank", "content", "thirdPartyPayAccount.accountNo:payAccount"};
        JSONObject body = propsFilter(one, JSON);
        ifNotNullThen(one.getPayType(), x -> enumToJson(x, body, "payWay"));
        ifNotNullThen(one.getUpdateDate(), x -> body.replace("payDate", ISO_DATETIME_FORMAT.format(x)));
        ifNotNullThen(one.getCreateDate(), x -> body.replace("createDate", ISO_DATETIME_FORMAT.format(x)));
        body.replace("area", parsePartnerArea(one).toString());
        parseRemark(one, body);

        return OK(body);
    }

    private StringBuffer parsePartnerArea(AccountFlow one) {
        PartnerApply partnerApply = one.getPartnerApply();
        StringBuffer sb = new StringBuffer();
        ifNotNullThen(partnerApply, x -> {
            ifNotNullThen(partnerApply.getProvince(), t -> sb.append(t.getName()));
            ifNotNullThen(partnerApply.getCity(), t -> sb.append("-").append(t.getName()));
            ifNotNullThen(partnerApply.getArea(), t -> sb.append("-").append(t.getName()));
            ifNotNullThen(partnerApply.getProvince2(), t -> sb.append(",").append(t.getName()));
            ifNotNullThen(partnerApply.getCity2(), t -> sb.append("-").append(t.getName()));
            ifNotNullThen(partnerApply.getArea2(), t -> sb.append("-").append(t.getName()));
        });
        return sb;
    }

    /**
     * 9.5.1 岗位招聘业务流水列表
     *
     * @Author mu.jie
     * @Date 2016/12/3
     */
    @RequestMapping(value = "/recruit", method = GET)
    public Result findRecruitList(BackPageVo backPageVo, String text, FlowState tableType, Date startTime, Date endTime,
                                  FlowState payStatus, PayType payWay, SourceType opType, RecruitmentType recruitType) {
        Page<AccountFlow> page = accountFlowService.findAccountFlowList(backPageVo, decodePathVariable(text), tableType,
                startTime, endTime, null, payWay, "job", payStatus, opType, recruitType);
        String[] JSON = {"id", "job.shop.name", "job.name", "job.recType.title", "job.reward",
                "source.title", "job.epmCount", "amount", "updateDate", "orderNo", "payType.title", "state"};
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, JSON);
            parseFlowState(x.getState(), jo, "state");
            ifNotNullThen(x.getCreateDate(), t -> jo.replace("updateDate", ISO_DATETIME_FORMAT.format(t)));
            return jo;
        }));
    }

    public static JSONObject parseFlowState(FlowState e, JSONObject body, String key) {
        if (e == null) return body;
        JSONObject jo = new JSONObject();
        jo.put("key", e);
        String text = e == APPROVAL ? "线下付款中" : e == SUCCESS ? "已付款" : "退款失败";
        jo.put("text", text);
        body.replace(key, jo);
        return body;
    }

    /**
     * 9.5.2 详情
     *
     * @Author mu.jie
     * @Date 2016/12/3
     */
    @RequestMapping(value = "/recruitInfo", method = GET)
    public Result recruitList(Long id) {
        AccountFlow one = accountFlowService.findOne(id);
        String[] JSON = {"job.shop.name:shopName", "job.name:jobName", "job.recType.title:recruitType", "amount:allMoney", "jobType",
                "job.epmCount:no", "job.reward:award", "updateDate:refundTime", "jobApplyRecord.receiver.realName:stuff", "jobApplyRecord.startDate:workTime",
                "updateDate:payTime", "payType:payWay", "account.member.realName:name", "payBank", "bankNO", "bankFlow", "receiveBank", "content", "orderNo:payFlow", "account.member.mobile:payAccount", "payStatus", "payOfflineType"};
        JSONObject body = propsFilter(one, JSON);
        enumToJson(one.getSource(), body, "jobType");
        enumToJson(one.getPayOfflineType(), body, "payOfflineType");
        enumToJson(one.getPayType(), body, "payWay");
        ifNotNullThen(one.getApplyDate(), x -> body.replace("refundTime", ISO_DATETIME_FORMAT.format(x)));
        ifNotNullThen(one.getUpdateDate(), x -> body.replace("payTime", ISO_DATETIME_FORMAT.format(x)));
        parseFlowState(one.getState(), body, "payStatus");
        parseRemark(one, body);
        return OK(body);
    }

    private void parseRemark(AccountFlow one, JSONObject body) {
        ifNotNullThen(one.getRemarks(), x -> {
            JSONObject jo = parseObject(x);
            body.replace("name", jo.get("username"));
            body.replace("payBank", jo.get("payBank"));
            body.replace("bankNO", jo.get("bankNO"));
            body.replace("bankFlow", jo.get("bankFlow"));
            body.replace("receiveBank", jo.get("receiveBank"));
            body.replace("content", jo.get("content"));
        });
    }

    /**
     * 9.5.3 已付款统计
     *
     * @Author mu.jie
     * @Date 2016/12/5
     */
    @RequestMapping(value = "/countRecruit", method = GET)
    public Result countRecruit(String text, Date startTime, Date endTime, FlowState payStatus, PayType payWay, SourceType opType) {
//        JOB_NEW("新开"), JOB_ADD("新增"), JOB_CUT("减少"), JOB_RENEW("续费"), JOB_RESIGN("退押金"), JOB_REFUND("退费")
        BigDecimal xkB = accountFlowService.countRecruit(decodePathVariable(text), startTime, endTime, payStatus, payWay, opType, JOB_NEW);
        BigDecimal xk = xkB != null ? xkB : ZERO;
        BigDecimal xzB = accountFlowService.countRecruit(decodePathVariable(text), startTime, endTime, payStatus, payWay, opType, JOB_ADD);
        BigDecimal xz = xzB != null ? xzB : ZERO;
        BigDecimal jsB = accountFlowService.countRecruit(decodePathVariable(text), startTime, endTime, payStatus, payWay, opType, JOB_CUT);
        BigDecimal js = jsB != null ? jsB : ZERO;
        BigDecimal xfB = accountFlowService.countRecruit(decodePathVariable(text), startTime, endTime, payStatus, payWay, opType, JOB_RENEW);
        BigDecimal xf = xfB != null ? xfB : ZERO;
        BigDecimal tyjB = accountFlowService.countRecruit(decodePathVariable(text), startTime, endTime, payStatus, payWay, opType, JOB_RESIGN);
        BigDecimal tyj = tyjB != null ? tyjB : ZERO;
        BigDecimal tfB = accountFlowService.countRecruit(decodePathVariable(text), startTime, endTime, payStatus, payWay, opType, JOB_REFUND);
        BigDecimal tf = tfB != null ? tfB : ZERO;
        BigDecimal count1 = xk.add(xz).add(xf);
        BigDecimal count2 = js.add(tyj).add(tf);
        JSONObject body = new JSONObject();
        body.put("staffingRevenue", count1);
        body.put("anewHiringFee", xk);
        body.put("increaseHiringCost", xz);
        body.put("postRenewalFee", xf);
        body.put("hiringRefund", count2);
        body.put("restFullRefundFee", tf);
        body.put("reduceHiringCost", js);
        body.put("refundDepositFee", tyj);
        return OK(body);
    }

    /**
     * 9.5.4 线下付款修改
     *
     * @Author mu.jie
     * @Date 2016/12/5
     */
    @RequestMapping(value = "/recruit", method = POST)
    public Result updateRecruit(Long id, FlowState payState, Date payTime, String receiver, PayOfflineType payWay, String payBank, String receiverAccount, String bankSerialNumber, String recipientBank, String reson) {
        accountFlowService.updateAccountFlow(id, payState, payTime, receiver, payWay, payBank, receiverAccount, bankSerialNumber, recipientBank, reson);
        return OK("success");
    }

    /**
     * 9.8.1 提现流水列表
     */
    @RequestMapping(value = "/withdraw", method = GET)
    public Result withdrawList(BackPageVo backPageVo, String text, FlowState tableType, Date startTime,
                               Date endTime, FlowState cashStatus, WithdrawType cashAccount) {
        Page<AccountFlow> page = accountFlowService.findWihtdrawList(backPageVo, text, tableType, startTime, endTime, cashStatus, cashAccount);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, WITHDRAW_LIST_JSON);
            ifNotNullThen(x.getCreateDate(), t -> jo.replace("createDate", ISO_DATETIME_FORMAT.format(t)));
            parseFlowState(x.getState(), jo, "payStatus");
            if (x.getState() == null) return null;
            JSONObject jsonObject = null;
            if (x.getState() != null) {
                jsonObject = new JSONObject();
                jsonObject.put("key", x.getState());
                String string = x.getState() == APPROVAL ? "待处理" : x.getState() == SUCCESS ? "提现成功" : "提现失败";
                jsonObject.put("text", string);
                jo.replace("payStatus", jsonObject);
            }
            return jo;
        }));
    }

    /**
     * 9.8.2 提现修改
     */
    @RequestMapping(value = "/withdraw", method = POST)
    public Result withdrawEdit(Long id, FlowState cashStatus, String bankFlow, Long payBank, Date payTime, String reson) {
        accountFlowService.editWithdraw(id, cashStatus, bankFlow, payBank, payTime, reson);
        return OK("success");
    }

    /**
     * 9.8.4 提现统计
     *
     * @Author mu.jie
     * @Date 2016/12/15
     */
    @RequestMapping(value = "/countWithdraw", method = GET)
    public Result countWithraw(String text, FlowState tableType, Date startTime,
                               Date endTime, FlowState cashStatus, WithdrawType cashAccount) {
        BigDecimal schoolCount = accountFlowService.countWithdraw(decodePathVariable(text), tableType, startTime, endTime, cashStatus, cashAccount, "SCHOOL");
        BigDecimal memberCount = accountFlowService.countWithdraw(decodePathVariable(text), tableType, startTime, endTime, cashStatus, cashAccount, "MEMBER");
        BigDecimal schoolB = schoolCount == null ? ZERO : schoolCount;
        BigDecimal memberB = memberCount == null ? ZERO : memberCount;
        JSONObject body = new JSONObject();
        body.put("schoolCount", schoolB);
        body.put("memberCount", memberB);
        body.put("totalCount", schoolB.add(memberB));
        return OK(body);
    }

    /**
     * 9.8.3 提现详情
     */
    @RequestMapping(value = "/withdrawDetail", method = GET)
    public Result withdrawDetail(Long id) {
        JSONObject body = new JSONObject();
        String[] withdraw_json = {
                "account.member.realName:username", "withdrawAccount.account:account", "amount:cashAmount", "createDate:applyTime", "userType"
        };
        AccountFlow accountFlow = accountFlowService.findOne(id);
        JSONObject jsonObject = propsFilter(accountFlow, withdraw_json);
        Member member = accountFlow.getAccount().getMember();
        enumToJson(member.getRole(), jsonObject, "userType");
        ifNotNullThen(accountFlow.getCreateDate(), x -> jsonObject.replace("applyTime", ISO_DATETIME_FORMAT.format(x)));
        body.put("withdrawDetail", jsonObject);

        String[] cashAccount_json = {"withdrawAccount.type.title:withdrawWay", "withdrawAccount.account:account", "withdrawAccount.member.realName:username"};
        JSONObject cashAccountJson = propsFilter(accountFlow, cashAccount_json);
        ifNotNullThen(accountFlow.getWithdrawAccount().getType(), t -> enumToJson(t, cashAccountJson, "withdrawWay"));
        ifTrueThen(accountFlow.getWithdrawAccount() != null && accountFlow.getWithdrawAccount().getType() == WITHDRAW_ACCOUNT_BANK_CARD, () -> {
            WithdrawAccount withdrawAccount = accountFlow.getWithdrawAccount();
            cashAccountJson.put("bankName", withdrawAccount.getBank());
            cashAccountJson.put("branchBankInfo", withdrawAccount.getBranchBank());

            JSONObject cardInfoJson = new JSONObject();
            if (withdrawAccount.getMember() != null) {
                MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(withdrawAccount.getMember().getToken());
                cardInfoJson.put("IDCardNo", memberExtInfo.getIdCardNo());
                cardInfoJson.put("IDCardFrontImg", memberExtInfo.getIdCard().getPath());
                cardInfoJson.put("IDCardHalfImg", memberExtInfo.getHalf().getPath());
            } else valueIsNull(cardInfoJson, null, "IDCardNo", "IDCardFrontImg", "IDCardHalfImg");
            body.put("cardInfo", cardInfoJson);
        });
        body.put("cashAccount", cashAccountJson);

        String[] base_member_info_json = {
                "headImage:headImg", "id:vipId", "mobile", "realName:username", "username:nickName", "gender.title:gender", "birthday",
                "hpAccount", "createTime:regDate", "referrer", "entrepreneurLevel.title:isEntrepreneurs", "partnerLevel.title:isPartner"
        };
        String[] base_school_info_json = {"accountId", "account", "member.realName:username", "member.gender:gender", "member.mobile:mobile",
                "member.role.title:accountType", "name", "location:adress", "xyz", "web", "regDate"};
        JSONObject baseInfomation = propsFilter(member, base_member_info_json);
        ifNotBlankThen(member.getUsername(), name -> baseInfomation.replace("nickName", decodePathVariable(name)));
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(member.getToken());
        ifNotNullThen(memberExtInfo.getRecommendUser(), x -> baseInfomation.replace("referrer", x.getRealName()));
        ifNotNullThen(member.getBirthday(), x -> baseInfomation.replace("birthday", ISO_DATE_FORMAT.format(x)));
        ifNotNullThen(member.getCreateTime(), x -> baseInfomation.replace("regTime", ISO_DATETIME_FORMAT.format(x)));
        body.put("cashAccountBaseInfo", baseInfomation);
        ifTrueThen(member.getRole() == ModuleKey.AccountEnum.SCHOOL, () -> {
            School school = schoolRepository.findByMemberId(member.getId());
            ifNotNullThen(school, t -> {
                JSONObject schoolJson = propsFilter(t, base_school_info_json);
                schoolJson.replace("accountId", accountFlow.getWithdrawAccount().getId());
                schoolJson.replace("account", accountFlow.getWithdrawAccount().getAccount());
                schoolJson.replace("xyz", t.getLongitude() + "," + t.getLatitude());
                schoolJson.replace("web", t.getLink());
                schoolJson.replace("regDate", ISO_DATE_FORMAT.format(t.getDate()));
                body.replace("cashAccountBaseInfo", schoolJson);
            });
        });

        JSONObject manageJson = new JSONObject();
        JSONObject resultJson = new JSONObject();
        resultJson.put("key", accountFlow.getState());
        resultJson.put("text", accountFlow.getState().getTitle());
        manageJson.put("result", resultJson);
        ifNotNullThen(accountFlow.getRemarks(), x -> {
            JSONObject jo = parseObject(x);
            manageJson.put("time", jo.get("payTime"));
            manageJson.put("flowNumber", jo.get("bankFlow"));
            manageJson.put("bank", jo.get("payBank"));
            manageJson.put("content", jo.get("remark"));
        });
        ifTrueThen(accountFlow.getRemarks() == null, () -> valueIsNull(manageJson, null, "time", "flowNumber", "bank", "content"));
        ifNotNullThen(accountFlow.getUpdateDate(), t -> manageJson.put("cashTime", ISO_DATETIME_FORMAT.format(t)));
        body.put("manage", manageJson);

        return OK(body);
    }


    /**
     * TODO:text搜索有问题
     * 9.9.1 平台收益流水列表
     */
    @RequestMapping(value = "/platformRevenue ", method = GET)
    public Result platformRevenue(BackPageVo pageVO, Date startTime, Date endTime, String earningsType, String text) throws ParseException {
        Page<AccountFlow> accountFlows = accountFlowService.platformRevenue(pageVO, startTime, endTime, earningsType, decodePathVariable(text));
        Page<JSONObject> jsonObject = accountFlows.map(e -> {
            JSONObject jo = propsFilter(e, PLATFORM_REVENUE_ACCOUNTFLOW_JSON);
            ifNotNullThen(e.getPartnerApply(), x -> {
                jo.replace("type", "合伙收益");
                jo.replace("source", x.getMember().getRealName());
            });
            ifNotNullThen(e.getEntrepreneursApply(), x -> {
                jo.replace("type", "创业者收益");
                jo.replace("source", x.getMember().getRealName());
            });
            ifTrueThen(e.getJob() != null && e.getSource() == PLATFORM_PROFIT_JOB, () -> {
                jo.replace("type", "岗位奖励收益");
                jo.replace("source", e.getJob().getShop().getOwner().getRealName());
            });
            ifTrueThen(e.getJob() != null && e.getSource() == DEFAULTS, () -> {
                jo.replace("type", "岗位违约金收益");
                jo.replace("source", e.getJob().getShop().getOwner().getRealName());
            });
            ifNotNullThen(e.getCourseApply(), x -> {
                jo.replace("type", "课程报名收益");
                jo.replace("source", x.getMember().getRealName());
            });
            ifNotNullThen(e.getCourseRefundApply(), x -> {
                jo.replace("type", "课程报名退款");
                jo.replace("source", x.getMember().getRealName());
            });
            if (e.getType() == PAY_IN) {
                jo.put("payType", "收益");
            } else jo.put("payType", "支出");

            ifNotNullThen(e.getCreateDate(), x -> jo.replace("dateTime", ISO_DATETIME_FORMAT.format(x)));
            return jo;
        });
        return OK(jsonObject);
    }


    /**
     * 9.9.2 收益合计
     */
    @RequestMapping(value = "/totalRevenue", method = GET)
    public Result totalRevenue(Date startTime, Date endTime, String text, String earningsType) {
        BigDecimal partner = accountFlowService.countRevenue(startTime, endTime, decodePathVariable(text), earningsType, "PARTNER");
        BigDecimal partnerB = isNull(partner) ? ZERO : partner;
        BigDecimal entrepreneurs = accountFlowService.countRevenue(startTime, endTime, decodePathVariable(text), earningsType, "ENTREPRENEURS");
        BigDecimal entrepreneursB = isNull(entrepreneurs) ? ZERO : entrepreneurs;
        BigDecimal jobIncome = accountFlowService.countRevenue(startTime, endTime, decodePathVariable(text), earningsType, "JOB_INCOME");
        BigDecimal jobIncomeB = isNull(jobIncome) ? ZERO : jobIncome;
        BigDecimal jobPenalty = accountFlowService.countRevenue(startTime, endTime, decodePathVariable(text), earningsType, "JOB_PENALTY");
        BigDecimal jobPenaltyB = isNull(jobPenalty) ? ZERO : jobPenalty;
        BigDecimal courseApply = accountFlowService.countRevenue(startTime, endTime, decodePathVariable(text), earningsType, "COURSE_APPLY");
        BigDecimal courseApplyB = isNull(courseApply) ? ZERO : courseApply;
        BigDecimal courseRefundApply = accountFlowService.countRevenue(startTime, endTime, decodePathVariable(text), earningsType, "COURSE_REFUND");
        BigDecimal courseRefundApplyB = isNull(courseRefundApply) ? ZERO : courseRefundApply;
        BigDecimal sum = partnerB.add(entrepreneursB).add(jobIncomeB).add(jobPenaltyB).add(courseApplyB).add(courseRefundApplyB);
        JSONObject body = new JSONObject();
        body.put("partner", partnerB);
        body.put("entrepreneurs", entrepreneursB);
        body.put("jobIncome", jobIncomeB);
        body.put("jobPenalty", jobPenaltyB);
        body.put("courseApply", courseApplyB);
        body.put("courseRefundApply", courseRefundApplyB);
        body.put("platform", sum);
        return OK(body);
    }


    /**
     * 9.9.3 详情
     */
    @RequestMapping(value = "/revenueDetail", method = GET)
    public Result revenueDetail(Long id) {
        JSONObject body = new JSONObject();
        AccountFlow accountFlow = accountFlowService.revenueDetail(id);
        //创业收益详情
        parsePlatformEntrepreneursApply(body, accountFlow);
        //合伙人收益详情
        parsePlatformPartner(body, accountFlow);
        //岗位奖励收益详情
        parsePlatformJobIncome(body, accountFlow);
        return OK(body);
    }

    private void parsePlatformJobIncome(JSONObject body, AccountFlow accountFlow) {
        if (accountFlow.getJob() != null) {
            JSONObject platformEarningsDetails = new JSONObject();
            JSONObject platformEarningsDetails_earingsDetails = parseEaringsDetails(accountFlow, "岗位奖励收益", "JOB_INCOME");
            String[] PLATFORMEARNINGSDETAILS_EARNINGSSOURCE_JSON = {"job.shop.name:shopName", "job.name:position", "job.recType.title:recruitType",
                    "job.reward:rewardAmount", "jobApplyRecord.receiver.realName:workStuff", "jobApplyRecord.startDate:joinTime"};
            JSONObject platformEarningsDetails_earningsSource = propsFilter(accountFlow, PLATFORMEARNINGSDETAILS_EARNINGSSOURCE_JSON);
            if (isNotNull(accountFlow.getJobApplyRecord())) {
                if (isNotNull(accountFlow.getJobApplyRecord().getStartDate()))
                    platformEarningsDetails_earningsSource.put("joinTime", ISO_DATETIME_FORMAT.format(accountFlow.getJobApplyRecord().getStartDate()));
                else platformEarningsDetails_earningsSource.put("joinTime", null);
            } else platformEarningsDetails_earningsSource.put("joinTime", null);

            List<AccountFlow> accountFlowList = accountFlowService.findJobIncomeFlowList(accountFlow.getJob());
            String[] joArr = {"id", "account.member.realName:username", "account.member.mobile:mobile", "amount", "createDate:time", "source.remark:relationship"};
            List<JSONObject> accountFlowJsonList = simpleMap(accountFlowList, x -> {
                JSONObject jo = propsFilter(x, joArr);
                ifNotNullThen(x.getCreateDate(), date -> jo.replace("time", ISO_DATETIME_FORMAT.format(date)));
                return jo;
            });
            platformEarningsDetails.put("earingsDetails", platformEarningsDetails_earingsDetails);
            platformEarningsDetails.put("earningsSource", platformEarningsDetails_earningsSource);
            platformEarningsDetails.put("track", accountFlowJsonList);
            body.put("platformEarningsDetails", platformEarningsDetails);
        }
    }

    private void parsePlatformPartner(JSONObject body, AccountFlow accountFlow) {
        if (isNotNull(accountFlow.getPartnerApply())) {
            JSONObject platformEarningsDetails = new JSONObject();//合伙人收益
            JSONObject platformEarningsDetails_earingsDetails = parseEaringsDetails(accountFlow, "合伙人收益", "PARTNER_AWARD");
            String[] PLATFORMEARNINGSDETAILS_EARNINGSSOURCE_JSON = {"partnerApply.member.realName:username", "partnerApply.member.mobile:mobile",
                    "partnerArea", "partnerApply.joinMoney:fee", "payType.title:payWay", "state.title:payStatus", "orderNo:payFlow", "updateDate:payTime",
                    "payUser", "payBank", "remark"};
            JSONObject platformEarningsDetails_earningsSource = propsFilter(accountFlow, PLATFORMEARNINGSDETAILS_EARNINGSSOURCE_JSON);
            platformEarningsDetails_earningsSource.replace("partnerArea", parsePartnerArea(accountFlow).toString());
            parseFlowState(accountFlow.getState(), platformEarningsDetails_earningsSource, "payStatus");
            ifNotNullThen(accountFlow.getUpdateDate(), date -> platformEarningsDetails_earningsSource.replace("payTime", ISO_DATETIME_FORMAT.format(date)));
            if (accountFlow.getPayType() == PAY_OFFLINE) {
                ifNotNullThen(accountFlow.getRemarks(), x -> {
                    JSONObject jo = parseObject(x);
                    platformEarningsDetails_earningsSource.replace("payUser", jo.get("username"));
                    platformEarningsDetails_earningsSource.replace("payBank", jo.get("payBank"));
                    platformEarningsDetails_earningsSource.replace("remark", jo.get("remark"));
                });
            } else {
                ifNotNullThen(accountFlow.getAccount().getMember(), x -> platformEarningsDetails_earningsSource.replace("payUser", x.getRealName()));
                ifNullThen(accountFlow.getAccount().getMember(), () -> platformEarningsDetails_earningsSource.replace("payUser", "平台"));
                platformEarningsDetails_earningsSource.replace("payBank", accountFlow.getAccount().getUid());
                platformEarningsDetails_earningsSource.replace("remark", accountFlow.getRemark());
            }

            platformEarningsDetails.put("earingsDetails", platformEarningsDetails_earingsDetails);
            platformEarningsDetails.put("earningsSource", platformEarningsDetails_earningsSource);
            body.put("platformEarningsDetails", platformEarningsDetails);
        }
    }

    private void parsePlatformEntrepreneursApply(JSONObject body, AccountFlow accountFlow) {
        if (isNotNull(accountFlow.getEntrepreneursApply())) {
            JSONObject platformEarningsDetails = new JSONObject();//创业收益
            JSONObject platformEarningsDetails_earingsDetails = parseEaringsDetails(accountFlow, "创业收益", "ENTREPRENEUR_AWARD");
            String[] PLATFORMEARNINGSDETAILS_EARINGSDETAILS_JSON = {"entrepreneursApply.member.realName:username", "entrepreneursApply.member.mobile:mobile", "entrepreneursApply.member.entrepreneurLevel.title:estpType",
                    "regReferer", "entrepreneursApply.joinMoney:fee", "payType.title:payWay", "state.title:payStatus", "orderNo:payFlow", "updateDate:payTime", "payUser", "payAccount"};
            JSONObject platformEarningsDetails_earningsSource = propsFilter(accountFlow, PLATFORMEARNINGSDETAILS_EARINGSDETAILS_JSON);
            parseFlowState(accountFlow.getState(), platformEarningsDetails_earningsSource, "payStatus");
            ifNotNullThen(accountFlow.getEntrepreneursApply(), x -> {
                MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(accountFlow.getEntrepreneursApply().getMember().getToken());
                ifNotNullThen(memberExtInfo, t -> platformEarningsDetails_earningsSource.replace("regReferer", t.getMember().getRealName()));
            });
            ifNotNullThen(accountFlow.getUpdateDate(), x -> platformEarningsDetails_earningsSource.replace("payTime", ISO_DATETIME_FORMAT.format(x)));
            if (accountFlow.getPayType() == PAY_OFFLINE) {
                ifNotNullThen(accountFlow.getRemarks(), x -> {
                    JSONObject jo = parseObject(x);
                    platformEarningsDetails_earningsSource.replace("payUser", jo.get("username"));
                    platformEarningsDetails_earningsSource.replace("payAccount", jo.get("payBank"));
                });
            } else {
                ifNotNullThen(accountFlow.getAccount().getMember(), x -> platformEarningsDetails_earningsSource.replace("payUser", x.getRealName()));
                ifNullThen(accountFlow.getAccount().getMember(), () -> platformEarningsDetails_earningsSource.replace("payUser", "平台"));
                platformEarningsDetails_earningsSource.replace("payAccount", accountFlow.getAccount().getUid());
            }
            List<AccountFlow> accountFlowList = accountFlowService.findEntrepreneursFlowList(accountFlow.getEntrepreneursApply());
            List<JSONObject> platformEarningsDetails_track = simpleMap(accountFlowList, x -> {
                String[] joArr = {"id", "account.member.realName:username", "account.member.mobile:mobile", "account.member.entrepreneurLevel.title:estpType", "amount", "createDate:time", "source.title:relationship"};
                JSONObject jo = propsFilter(x, joArr);
                ifNotNullThen(x.getCreateDate(), t -> jo.replace("time", ISO_DATETIME_FORMAT.format(t)));
                return jo;
            });


            platformEarningsDetails.put("track", platformEarningsDetails_track);
            platformEarningsDetails.put("earingsDetails", platformEarningsDetails_earingsDetails);
            platformEarningsDetails.put("earningsSource", platformEarningsDetails_earningsSource);
            body.put("platformEarningsDetails", platformEarningsDetails);
        }
    }

    private JSONObject parseEaringsDetails(AccountFlow accountFlow, String text, String value) {
        JSONObject json = new JSONObject();
        JSONObject earningTypeJson = new JSONObject();
        earningTypeJson.put("text", text);
        earningTypeJson.put("value", value);
        json.put("earningsType", earningTypeJson);
        json.put("earnings", accountFlow.getAmount());
        json.put("time", ISO_DATE_FORMAT.format(accountFlow.getCreateDate()));
        return json;
    }

    /**
     * 9.14.1 课程报名业务流水列表
     *
     * @Author mu.jie
     * @Date 2017/2/22
     */
    @RequestMapping(value = "/courseApplyList", method = GET)
    public Result findCourseApplyFlow(BackPageVo backPageVo, String text, FlowState tableType, Date startTime, Date endTime, String userToken) {
        String[] json = {"id", "orderNo", "courseApply.member.mobile", "courseApply.member.realName", "courseApply.course.name",
                "courseApply.price", "courseApply.discount", "amountPayable", "createDate", "updateDate", "payType", "state"};
        Member member = memberService.findByToken(userToken);
        Page<AccountFlow> page = accountFlowService.
                findCourseApplyAccountFlowList(backPageVo.pageRequest(), decodePathVariable(text), tableType, startTime, endTime, member);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, json);
            jo.put("userType", member.getRole());
            ifNotNullThen(x.getState(), t -> enumToJson(t, jo, "state"));
            ifNotNullThen(x.getPayType(), t -> enumToJson(t, jo, "payType"));
            ifNotNullThen(x.getCreateDate(), t -> jo.replace("createDate", ISO_DATETIME_FORMAT.format(t)));
            ifNotNullThen(x.getUpdateDate(), t -> jo.replace("updateDate", ISO_DATETIME_FORMAT.format(t)));
            ifNotNullThen(x.getCourseApply(), t -> jo.replace("amountPayable", t.getPrice().subtract(t.getDiscount())));
            return jo;
        }));
    }

    /**
     * 9.14.2 已付款统计
     *
     * @Author mu.jie
     * @Date 2017/3/7
     */
    @RequestMapping(value = "/countCourseApply", method = GET)
    public Result countInfo(String text, Date startTime, Date endTime) {
        Account platform = accountService.findZuesAccount();
        BigDecimal schoolIncome = accountFlowService.countCourseApplySchool(decodePathVariable(text), startTime, endTime, platform);
        BigDecimal schoolIncomeB = schoolIncome == null ? ZERO : schoolIncome;
        BigDecimal platformIncome = accountFlowService.countCourseApplyPlatform(decodePathVariable(text), startTime, endTime, platform);
        BigDecimal platformIncomeB = platformIncome == null ? ZERO : platformIncome;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("totalIncome", schoolIncomeB.add(platformIncomeB));
        jsonObject.put("schoolIncome", schoolIncomeB);
        jsonObject.put("platFormIncome", platformIncomeB);
        return OK(jsonObject);
    }

    /**
     * 9.14.3 详情
     *
     * @Author mu.jie
     * @Date 2017/2/22
     */
    @RequestMapping(value = "/courseApplyInfo", method = GET)
    public Result findCourseApplyFlowInfo(Long id) {
        AccountFlow one = accountFlowService.findOne(id);
        JSONObject body = new JSONObject();
        JSONObject courseApplyInfo = parseCourseInfo2Json(one);
        body.put("courseInfo", courseApplyInfo);

        JSONObject signUpInfo = parseSingUpInfo2Json(one);
        body.put("signUpInfo", signUpInfo);

        JSONObject payDetails = parsePayDetails(one);
        body.put("payDetails", payDetails);

        return OK(body);
    }

    private JSONObject parseCourseInfo2Json(AccountFlow one) {
        String[] COURSE_APPLY_INFO_JSON = {
                "courseApply.course.school.name:schoolName", "courseApply.course.name:courseName",
                "courseApply.course.isPlatformCourse:isPartnerCourse", "courseApply.course.platformPercent:platFormShareRate",
                "courseApply.course.redPacketPercent:redPacketUseRate", "courseApply.course.price:amount", "courseApply.course.day:days"
        };
        JSONObject jo = propsFilter(one, COURSE_APPLY_INFO_JSON);
        CourseApply courseApply = one.getCourseApply();
        ifNotNullThen(courseApply.getCourse().getIsPlatformCourse(), e -> enumToJson(e, jo, "isPartnerCourse"));
        return jo;
    }

    private JSONObject parseSingUpInfo2Json(AccountFlow one) {
        String[] COURSE_APPLY_INFO_INFO_JSON = {
                "courseApply.member.mobile:mobile", "courseApply.member.realName:username",
                "courseApply.member.hpAccount:hpAccount", "courseApply.serialNo:orderNo", "courseApply.trainDate:trainTime",
                "courseApply.price:totalAmount", "couponWay", "redPacketUseNum", "redPacketAmount", "courseApply.discount:discount",
                "courseApply.price:amountPayable", "schoolIncome", "platFormIncome", "orderStatus",
                "courseApply.date:signUpTime", "courseApply.payType:payWay", "courseApply.payDate:payTime"
        };
        JSONObject jo = propsFilter(one, COURSE_APPLY_INFO_INFO_JSON);
        ifNotNullThen(one.getCourseApply().getTrainDate(), d -> jo.replace("trainTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getCourseApply().getDate(), d -> jo.replace("signUpTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getCourseApply().getPayDate(), d -> jo.replace("payTime", ISO_DATETIME_FORMAT.format(d)));
        CourseApply courseApply = one.getCourseApply();
        List<RedPacket> redPacketList = new ArrayList<>();
        courseApply.getRedPacketReceives().forEach(redPacketReceive -> redPacketList.add(redPacketReceive.getRedPacket()));
        if (redPacketList != null && redPacketList.size() > 0) {
            JSONObject couponWay = new JSONObject();
            couponWay.put("key", "REDPACKET");
            couponWay.put("text", "红包");
            jo.replace("couponWay", couponWay);
            jo.replace("redPacketUseNum", redPacketList.size());
            List<BigDecimal> redPackets = newArrayList();
            redPacketList.forEach(x -> redPackets.add(x.getAmount()));
            jo.replace("redPacketAmount", redPackets.toArray());
        } else if (courseApply.getIsUseFee() == ModuleKey.BooleanEnum.YES) {
            JSONObject couponWay = new JSONObject();
            couponWay.put("key", "FEE");
            couponWay.put("text", "免费培训");
            jo.replace("couponWay", couponWay);
        }

        BigDecimal money = courseApply.getPrice().subtract(courseApply.getDiscount());//用户付的钱
        BigDecimal schoolMoney = money.multiply(new BigDecimal(1d - (courseApply.getCourse().getPlatformPercent() / 100) + ""));//学校应该分的钱
        BigDecimal platformMoney = money.subtract(schoolMoney);//平台应该分的钱
        jo.replace("platFormIncome", platformMoney);
        jo.replace("schoolIncome", schoolMoney);
        enumToJson(one.getCourseApply().getState(), jo, "orderStatus");
        return jo;
    }

    private JSONObject parsePayDetails(AccountFlow one) {
        String[] json = {"createDate:submitPayTime", "payWay", "payStatus", "orderNo:payFlow", "updateDate:payTime",
                "payPathWay", "bankName", "bankFlow", "remark"};
        JSONObject jo = propsFilter(one, json);
        ifNotNullThen(one.getCreateDate(), d -> jo.replace("submitPayTime", ISO_DATETIME_FORMAT.format(d)));
        ifNotNullThen(one.getUpdateDate(), d -> jo.replace("payTime", ISO_DATETIME_FORMAT.format(d)));
        enumToJson(one.getPayType(), jo, "payWay");
        enumToJson(one.getState(), jo, "payStatus");
        enumToJson(one.getPayOfflineType(), jo, "payPathWay");
        ifNotBlankThen(one.getRemarks(), x -> {
            JSONObject jsonObject = parseObject(x);
            jo.replace("bankName", jsonObject.get("payBank"));
            jo.replace("bankFlow", jsonObject.get("bankFlow"));
            jo.replace("remark", jsonObject.get("content"));
        });
        return jo;
    }

    /**
     * 9.14.4 编辑
     *
     * @Author mu.jie
     * @Date 2017/2/22
     */
    @RequestMapping(value = "/courseApply", method = POST)
    public Result updateCourseApplyAccountFlow(Long id, FlowState payStatus, Date payTime, PayOfflineType payPathWay,
                                               String bank, String bankFlow, String remark) {
        accountFlowService.updateAccountFlow(id, payStatus, payTime, null, payPathWay, bank, null, bankFlow, null, remark);
        return OK("success");
    }

    /**
     * 9.15.1 学校提现流水列表
     *
     * @Author mu.jie
     * @Date 2017/2/23
     */
    @RequestMapping(value = "/schoolWithdraw", method = GET)
    public Result findSchoolWithdraw(BackPageVo backPageVo, String text, String userToken) {
        String[] json = {"id", "withdrawAccount.school.name", "withdrawAccount.type.title", "createDate", "state", "amount"};
        Member member = memberService.findByToken(userToken);
        Page<AccountFlow> page = accountFlowService.findSchoolWithdrawAccountFlow(backPageVo, decodePathVariable(text), member);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, json);
            ifNotNullThen(x.getCreateDate(), t -> jo.replace("createDate", ISO_DATETIME_FORMAT.format(t)));
            ifNotNullThen(x.getState(), t -> enumToJson(t, jo, "state"));
            return jo;
        }));
    }

    /**
     * 9.15.2 详情
     *
     * @Author mu.jie
     * @Date 2017/2/23
     */
    @RequestMapping(value = "/schoolWithdrawInfo", method = GET)
    public Result findSchoolWithdrawInfo(Long id) {
        String[] json = {"withdrawAccount.school.name:schoolName", "amount:withDrawAmount", "withdrawAccount.type.title:accountType",
                "createDate:withDrawTime", "state", "updateDate:payTime", "remarks"};
        AccountFlow one = accountFlowService.findOne(id);
        JSONObject body = propsFilter(one, json);
        ifNotNullThen(one.getCreateDate(), t -> body.replace("withDrawTime", ISO_DATETIME_FORMAT.format(t)));
        ifNotNullThen(one.getUpdateDate(), t -> body.replace("payTime", ISO_DATETIME_FORMAT.format(t)));
        ifNotNullThen(one.getState(), t -> enumToJson(t, body, "state"));
        System.out.println(body.toJSONString());
        return OK(body);
    }

}
