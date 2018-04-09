package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.BenefitApply;
import com.thousandsunny.service.model.BenefitItem;
import com.thousandsunny.service.model.BenefitRel;
import com.thousandsunny.service.service.BenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.BenefitApplyState.NONE;
import static com.thousandsunny.service.ModuleKey.BenefitType.*;
import static java.sql.Date.valueOf;
import static java.time.LocalDate.now;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.time.DateUtils.addYears;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2016/11/19.
 */
@RestController
@RequestMapping(value = "/api/portal/benefit", produces = APPLICATION_JSON_UTF8_VALUE)
public class BenefitController {

    private static final String[] BENEFITREL_JSON = {"valid:isEffective", "effectiveDate"};

    @Autowired
    private BenefitService benefitService;
    private static final String[] BENEFIT_ITEM_JSON = new String[]{"id", "type", "state", "invalidDate:expireDate"};

    /**
     * 12.10上班好处-车旅费
     *
     * @Author mu.jie
     * @Date 2016/11/19
     */
    @RequestMapping(value = "/carFee", method = GET)
    public ResponseEntity carFeeBenefit(String userToken) {
        BenefitRel benefitRel = benefitService.findBenefitRel(userToken, CAR_FEE);
        JSONObject body = propsFilter(benefitRel, BENEFITREL_JSON);
        body.put("flowsId", isNotNull(benefitRel) && isNotNull(benefitRel.getFlow()) ? benefitRel.getFlow().getId() : null);
        if (isNotNull(benefitRel) && benefitRel.getEffectiveDate() != null) {
            body.replace("isEffective", valueOf(now()).getTime() > benefitRel.getEffectiveDate().getTime()
                    && valueOf(now()).getTime() < addYears(benefitRel.getEffectiveDate(), 1).getTime());
        } else body.replace("isEffective", false);
        return ok(body);
    }

    /**
     * 12.11上班好处-工作意外险
     *
     * @Author mu.jie
     * @Date 2016/11/19
     */
    @RequestMapping(value = "/workInsurance", method = GET)
    public ResponseEntity workInsuranceBenefit(String userToken) {
        BenefitRel benefitRel = benefitService.findBenefitRel(userToken, WORK_INSURANCE);
        JSONObject body = propsFilter(benefitRel, BENEFITREL_JSON);
        ifNotNullThen(benefitRel, x -> body.replace("isEffective", x.getValid().getBool()));
        ifNullThen(benefitRel, () -> body.replace("isEffective", false));
        return ok(body);
    }

    /**
     * 12.12上班好处-免费培训列表
     *
     * @Author mu.jie
     * @Date 2016/11/19
     */
    @RequestMapping(value = "/freeTrain", method = GET)
    public ResponseEntity freeTrain(String userToken) {
        List<BenefitItem> benefitItems = benefitService.findBenefitItem(userToken);
        List<JSONObject> body = simpleMap(benefitItems, x -> propsFilter(x, BENEFIT_ITEM_JSON));
        if (body == null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("list", newArrayList());
            return ok(jsonObject);
        }
        return ok(listToJson(body));
    }

    /**
     * 12.13上班好处-工资保障
     *
     * @Author mu.jie
     * @Date 2016/11/21
     */
    @RequestMapping(value = "/salaryProtection", method = GET)
    public ResponseEntity salaryProtection(String userToken) {
        BenefitRel benefitRel = benefitService.findBenefitRel(userToken, SALARY_PROTECTION);
        JSONObject body = parseBenefitRel(benefitRel, SALARY_PROTECTION);
        return ok(body);
    }

    /**
     * 12.14上班好处-工资保障-兑换保障
     *
     * @Author mu.jie
     * @Date 2016/11/21
     */
    @RequestMapping(value = "/applySalary", method = POST)
    public ResponseEntity applySalaryProtection(String userToken, BenefitApply benefitApply, String explain) {
        benefitService.addBenefitApply(userToken, SALARY_PROTECTION, benefitApply, explain);
        return OK;
    }

    /**
     * 12.15上班好处-工资保障-兑换保障提醒
     *
     * @Author mu.jie
     * @Date 2016/11/21
     */
    @RequestMapping(value = "/remindSalary", method = POST)
    public ResponseEntity remindSalaryProtection(String userToken) {
        benefitService.remindApply(userToken, SALARY_PROTECTION);
        return OK;
    }

    /**
     * 12.16上班好处-快速贷款
     *
     * @Author mu.jie
     * @Date 2016/11/21
     */
    @RequestMapping(value = "/quickLoan", method = GET)
    public ResponseEntity quickLoan(String userToken) {
        BenefitRel benefitRel = benefitService.findBenefitRel(userToken, QUICK_LOAN);
        JSONObject body = parseBenefitRel(benefitRel, QUICK_LOAN);
        return ok(body);
    }

    /**
     * 12.17上班好处-快速贷款-申请
     *
     * @Author mu.jie
     * @Date 2016/11/21
     */
    @RequestMapping(value = "/applyLoan", method = POST)
    public ResponseEntity applyQuickLoan(String userToken, BenefitApply benefitApply) {
        benefitService.addBenefitApply(userToken, QUICK_LOAN, benefitApply, null);
        return OK;
    }

    /**
     * 12.18上班好处-快速贷款-申请提醒
     *
     * @Author mu.jie
     * @Date 2016/11/21
     */
    @RequestMapping(value = "/remindLoan", method = POST)
    public ResponseEntity remindQuickLoan(String userToken) {
        benefitService.remindApply(userToken, QUICK_LOAN);
        return OK;
    }

    private JSONObject parseBenefitRel(BenefitRel benefitRel, ModuleKey.BenefitType type) {
        JSONObject body = propsFilter(benefitRel, BENEFITREL_JSON);
        ifNotNullThen(benefitRel, x -> {
            BenefitApply apply = benefitService.findBenefitApply(benefitRel.getMember(), type);
            body.put("applyState", apply != null ? apply.getApplyState() : NONE);
            body.put("isTodayRemind", apply != null ? apply.getIsTodayRemind().getBool() : false);
        });
        ifNullThen(benefitRel, () -> {
            body.put("applyState", NONE);
            body.put("isTodayRemind", false);
        });
        ifNotNullThen(benefitRel, x -> body.replace("isEffective", x.getValid().getBool()));
        ifNullThen(benefitRel, () -> body.replace("isEffective", false));
        return body;
    }
}
