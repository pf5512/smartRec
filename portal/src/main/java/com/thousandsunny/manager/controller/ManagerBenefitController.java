package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey.BenefitApplyState;
import com.thousandsunny.service.model.BenefitApply;
import com.thousandsunny.service.service.BenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.service.ModuleKey.BenefitType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by mu.jie on 2016/12/14.
 */
@RestController
@RequestMapping(value = "/api/manager/benefit", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerBenefitController {
    @Autowired
    private BenefitService benefitService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;

    /**
     * 7.1.1 工资保障列表
     *
     * @Author mu.jie
     * @Date 2016/12/14
     */
    @RequestMapping(value = "/salaryProtection", method = GET)
    public Result findSalaryProtection(BackPageVo backPageVo, String text, BenefitApplyState tableType) {
        String[] JSON = {"id", "member.realName", "member.hpAccount", "storeName", "date", "applyState", "remindDate"};
        Page<BenefitApply> page = benefitService.findSalaryProtection(backPageVo, decodePathVariable(text), tableType, SALARY_PROTECTION);
        return OK(parseBenefitApply(JSON, page));
    }

    /**
     * 7.1.2 删除
     *
     * @Author mu.jie
     * @Date 2016/12/14
     */
    @RequestMapping(value = "/salaryProtection", method = PUT)
    public Result deleteSalaryProtection(String id) {
        benefitService.deleteSalaryProtection(id);
        return OK("success");
    }

    /**
     * 7.1.3 修改
     *
     * @Author mu.jie
     * @Date 2016/12/14
     */
    @RequestMapping(value = "/salaryProtection", method = POST)
    public Result updateSalaryProtection(Long id, BenefitApplyState status, String remark) {
        benefitService.updateSalaryProtection(id, status, remark);
        return OK("success");
    }

    /**
     * 7.1.4 详情
     *
     * @Author mu.jie
     * @Date 2016/12/14
     */
    @RequestMapping(value = "/salaryProtectionInfo", method = GET)
    public Result salaryProtectionInfo(Long id) {
        String[] SUBMITDETAIL_JSON = {"name", "phoneNumber:tel", "storeName:shopname", "imgs", "date:submitTime", "reason:content"};
        BenefitApply one = benefitService.findBenefitApply(id);
        JSONObject body = new JSONObject();
        JSONObject submitDetailJson = propsFilter(one, SUBMITDETAIL_JSON);
        List<JSONObject> imgJson = simpleMap(one.getPics(), x -> propsFilter(x, "path:img"));
        ifNotNullThen(one.getDate(), t -> submitDetailJson.replace("submitTime", ISO_DATETIME_FORMAT.format(t)));
        submitDetailJson.replace("imgs", imgJson);
        body.put("submitDetail", submitDetailJson);

        parseBenefitApplyInfo(one, body);

        return OK(body);
    }

    private void parseBenefitApplyInfo(BenefitApply one, JSONObject body) {
        String[] BASEINFORMATION_JSON = {"member.headImage.path:headImg", "member.id:VIPid", "member.mobile:tel", "member.realName:name", "member.username:nickName",
                "member.gender.title:sex", "member.birthday:birthday", "member.hpAccount:hpAccount", "referrer", "member.createTime:regtime", "isCYZ", "ishhr"};
        JSONObject baseInformationJson = propsFilter(one, BASEINFORMATION_JSON);
        ifNotNullThen(one.getMember().getUsername(), t -> baseInformationJson.replace("nickName", decodePathVariable(t)));
        ifNotNullThen(one.getMember().getBirthday(), t -> baseInformationJson.replace("birthday", ISO_DATETIME_FORMAT.format(t)));
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(one.getMember().getToken());
        ifNotNullThen(memberExtInfo.getRecommendUser(), x -> baseInformationJson.replace("referrer", x.getRealName()));
        ifNotNullThen(one.getMember().getEntrepreneurLevel(), x -> baseInformationJson.replace("isCYZ", x == ModuleKey.IdentityType.NONE ? "否" : "是"));
        ifNotNullThen(one.getMember().getIdentityHasPass(), x -> baseInformationJson.replace("isCYZ", x == ModuleKey.BooleanEnum.YES ? "是" : "否"));
        body.put("baseInformation", baseInformationJson);

        List<JSONObject> timeJsons = simpleMap(one.getRemindDates(), x -> {
            JSONObject jo = new JSONObject();
            jo.put("time", ISO_DATETIME_FORMAT.format(x));
            return jo;
        });
        body.put("remindTimeRecord", timeJsons);

        String[] RESULT_JSON = {"state", "remark:content"};
        JSONObject resultJson = propsFilter(one, RESULT_JSON);
        enumToJson(one.getApplyState(), resultJson, "state");
        body.put("result", resultJson);
    }

    @RequestMapping(value = "/quickLoan", method = GET)
    public Result findQuickLoanList(BackPageVo backPageVo, String text, BenefitApplyState tableType) {
        String[] JSON = {"id", "member.realName", "member.hpAccount", "amount", "date", "applyState", "remindDate"};
        Page<BenefitApply> page = benefitService.findSalaryProtection(backPageVo, decodePathVariable(text), tableType, QUICK_LOAN);
        return OK(parseBenefitApply(JSON, page));
    }

    @RequestMapping(value = "/quickLoan", method = PUT)
    public Result delQuickLoan(String id) {
        benefitService.deleteSalaryProtection(id);
        return OK("success");
    }

    @RequestMapping(value = "/quickLoan", method = POST)
    public Result updateQuickLoan(Long id, BenefitApplyState status, String remark) {
        benefitService.updateSalaryProtection(id, status, remark);
        return OK("success");
    }

    @RequestMapping(value = "/quickLoanInfo", method = GET)
    public Result quickLoanInfo(Long id) {
        BenefitApply one = benefitService.findBenefitApply(id);
        String[] SUBMITDETAIL_JSON = {"name", "phoneNumber:tel", "date:submitTime", "amount:loanAmount"};
        JSONObject body = new JSONObject();
        JSONObject submitDetailJson = propsFilter(one, SUBMITDETAIL_JSON);
        ifNotNullThen(one.getDate(), t -> submitDetailJson.replace("submitTime", ISO_DATETIME_FORMAT.format(t)));
        body.put("submitDetail", submitDetailJson);

        parseBenefitApplyInfo(one, body);
        return OK(body);
    }

    private Page<JSONObject> parseBenefitApply(String[] JSON, Page<BenefitApply> page) {
        return page.map(x -> {
            JSONObject jo = propsFilter(x, JSON);
            enumToJson(x.getApplyState(), jo, "applyState");
            ifNotNullThen(x.getDate(), t -> jo.replace("date", ISO_DATETIME_FORMAT.format(t)));
            ifNotEmptyThen(x.getRemindDates(), t -> {
                simpleSort(t, (m, n) -> m.getDate().compareTo(n.getDate()));
                jo.replace("remindDate", ISO_DATETIME_FORMAT.format(t.get(0)));
            });
            return jo;
        });
    }

}
