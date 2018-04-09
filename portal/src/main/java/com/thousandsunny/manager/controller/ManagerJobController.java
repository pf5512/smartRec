package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.service.model.JobApplyRecord;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.JobApplyRecordService;
import com.thousandsunny.service.service.MemberRecRelService;
import com.thousandsunny.thirdparty.domain.service.AccountService;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThen;
import static com.thousandsunny.core.ModuleKey.SubLevelType;
import static com.thousandsunny.service.ModuleKey.RecState.WORKING;
import static com.thousandsunny.service.ModuleKey.RecruitmentType;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * 如果这些代码有用，那它们是guitarist在14/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@RestController
@RequestMapping(value = "/api/manager/jobs", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerJobController {

    private static final String[] ENTREPRENEURS_REWARD_JSON = {
            "id",
            "job.shop.name",
            "job.name",
            "job.recType.title",
            "job.reward",
            "jobApplyRecord.receiver.realName",
            "jobApplyRecord.startDate",
            "relationship",
            "account.member.realName",
            "account.member.hpAccount",
            "amount",
            "createDate",
            "jobApplyRecord.receiver.hpAccount",
            "relation.title:relationship"
    };
    @Autowired
    private AccountFlowService accountFlowService;
    @Autowired
    private AccountService accountService;

    /**
     * 9.6.1 岗位奖励明细列表
     */
    @RequestMapping(value = "/rewards", method = GET)
    public Result entrepreneursRewardDetail(Date startTime, Date endTime, RecruitmentType recruitType, String text, BackPageVo pageVo) {
        Account platform = accountService.findZuesAccount();
        Page<AccountFlow> page = accountFlowService.periodRecruitTypeAndKeyWord(startTime, endTime, recruitType, decodePathVariable(text), pageVo.pageRequest());
        return OK(page.map(a -> {
            JSONObject jsonObject = propsFilter(a, ENTREPRENEURS_REWARD_JSON);
            ifNotNullThen(a.getCreateDate(), t -> jsonObject.replace("createDate", ISO_DATETIME_FORMAT.format(t)));
            ifTrueThen(a.getJobApplyRecord().getStartDate() != null && a.getJobApplyRecord().getStartDate() != null,
                    () -> jsonObject.replace("jobApplyRecord.startDate", ISO_DATETIME_FORMAT.format(a.getJobApplyRecord().getStartDate())));
            if (a.getRelation() != null) {
                jsonObject.replace("relationship", a.getRelation().getTitle());
            } else if (a.getAccount().getId().equals(platform.getId())) {
                jsonObject.replace("jrelationship", "平台推荐收益");
            } else jsonObject.replace("relationship", "无推荐");
            return jsonObject;
        }));
    }
}

