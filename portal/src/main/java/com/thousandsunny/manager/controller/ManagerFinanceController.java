package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.repository.MemberExtInfoRepository;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.service.ManagerFinanceService;
import com.thousandsunny.thirdparty.ModuleKey.FlowState;
import com.thousandsunny.thirdparty.ModuleKey.PayType;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.service.ModuleKey.EntrepreneursType;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.ENTREPRENEUR_APPLY;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.PARTNER_APPLY;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/manager/finance", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerFinanceController {
    private static final String[] accountFlow_list_json = {
            "id",
            "account.member.realName:username",
            "account.member.hpAccount:hpAccount",
            "orderNo:payFlow",
            "regUser"
    };
    @Autowired
    private ManagerFinanceService managerFinanceService;
    @Autowired
    private MemberExtInfoRepository memberExtInfoRepository;

    /**
     * 9.2.1 创业者付款流水列表
     */
    @RequestMapping(value = "/eApplyFlows", method = GET)
    public Result listEntrepreneurApplyFlows(BackPageVo backPageVo, String text, FlowState tableType, Date startTime, Date endTime, PayType payWay, EntrepreneursType ESTPType) {
        Page<AccountFlow> accountFlows = managerFinanceService.applyFlows(backPageVo.pageRequest(), decodePathVariable(text), tableType, startTime, endTime, payWay, ENTREPRENEUR_APPLY, ESTPType);
        Page<JSONObject> jsonObjects = accountFlows.map(e -> {
            JSONObject jo = propsFilter(e, accountFlow_list_json);
            MemberExtInfo memberExtInfo = memberExtInfoRepository.findByMemberToken(e.getAccount().getMember().getToken());

            ifNotNullThen(memberExtInfo.getRecommendUser(), regUser -> jo.put("regUser", regUser.getRealName()));

            jo.put("payDate", ISO_DATETIME_FORMAT.format(e.getUpdateDate()));

            JSONObject payStatus = new JSONObject();
            payStatus.put("key", e.getState());
            payStatus.put("text", e.getState().getTitle());
            jo.put("payStatus", payStatus);

            JSONObject _payWay = new JSONObject();
            _payWay.put("key", e.getPayType());
            _payWay.put("text", e.getPayType().getTitle());
            jo.put("payWay", _payWay);
            return jo;
        });
        return OK(jsonObjects);
    }

    /**
     * 9.2.2 已付款统计(创业者)
     * fixme 9.2.2 已付款统计(创业者)
     */
    @RequestMapping(value = "/eApplyPaysCount", method = GET)
    public ResponseEntity countEntrepreneurApplyPays(String text, Date startTime, Date endTime, PayType payWay, EntrepreneursType ESTPType) {
//        BigDecimal data = managerFinanceService.countPays(text, startTime, endTime, payWay, PARTNER_APPLY, ESTPType);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", "300000.00");
        jsonObject.put("cj", "100000.00");
        jsonObject.put("gj", "80000.00");
        jsonObject.put("csg", "120000.00");
        return ok(jsonObject);
    }

    /**
     * 9.4.1 合伙人付款流水列表
     */
    @RequestMapping(value = "/pApplyFlows", method = GET)
    public Result listPartnerApplyFlows(BackPageVo backPageVo, String text, FlowState tableType, Date startTime, Date endTime, PayType payWay) {
        Page<AccountFlow> accountFlows = managerFinanceService.applyFlows(backPageVo.pageRequest(), decodePathVariable(text), tableType, startTime, endTime, payWay, PARTNER_APPLY, null);
        Page<JSONObject> jsonObjects = accountFlows.map(e -> {
            JSONObject jo = propsFilter(e, accountFlow_list_json);
            jo.put("area", e.getPartnerApply().getPlace());
            jo.put("payDate", ISO_DATETIME_FORMAT.format(e.getUpdateDate()));
            JSONObject j1 = new JSONObject();
            j1.put("key", e.getState());
            j1.put("text", e.getState().getTitle());
            jo.put("payStatus", j1);
            JSONObject j2 = new JSONObject();
            j2.put("key", e.getPayType());
            j2.put("text", e.getPayType().getTitle());
            jo.put("payWay", j2);
            return jo;
        });
        return OK(jsonObjects);
    }

    /**
     * 9.4.2 已付款统计(合伙人)
     * fixme 9.4.2 已付款统计(合伙人)
     */
    @RequestMapping(value = "/pPaysCount", method = GET)
    public ResponseEntity countPartnerApplyPays(String text, Date startTime, Date endTime, PayType payWay) {
//        BigDecimal data = managerFinanceService.countPays(text, startTime, endTime, payWay, PARTNER_APPLY, null);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", "300000.00");
        return ok(jsonObject);
    }
}
