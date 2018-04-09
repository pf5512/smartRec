package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.common.DateUtil;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey.WorkerQueryType;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.domain.service.ThirdPartyPayAccountService;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.IdentityType.NONE;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.core.ModuleKey.SubLevelType;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.ONCE;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType.PAY_OFFLINE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static com.thousandsunny.service.ModuleTips.TIP_NO_EARNING;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType.ENTREPRENEUR_AWARD;

@RestController
@RequestMapping(value = "/api/portal/entrepreneurs", produces = APPLICATION_JSON_UTF8_VALUE)
public class EntrepreneursController {

    private final static String[] APPLY_INFO = {
            "type:applyType",
            "state:applyState",
            "reviewDate",
            "notes:reviewRemark"
    };

    private final static String[] REWARD_LIST = {
            "id",
            "beEntrepreneurs.member.realName:realName",
            "date",
            "beEntrepreneurs.mobile:phoneNumber",
            "beEntrepreneurs.member.entrepreneurLevel:type",
            "amount"
    };

    private final static String[] REWARD_LISTS = {
            "id",
            "entrepreneursApply.member.realName:realName",
            "entrepreneursApply.member.mobile:phoneNumber",
            "amount",
            "entrepreneursApply.member.entrepreneurLevel:type",
    };

    private final static String[] EARNING_LISTS = {
            "id",
            "source:type",
            "createDate:date",
            "amount",
            "jobApplyRecord.job.jobType.name:jobType",
            "jobApplyRecord.job.reward:rewardAmount"
    };

    private final static String[] EARNING_INFOS = {
            "id",
            "source:type",
            "createDate:date",
            "amount",
            "jobApplyRecord.job.recType:jobType",
            "jobApplyRecord.job.reward:rewardAmount",
            "jobApplyRecord.job.id:jobId",
            "jobApplyRecord.job.name:position",
            "jobApplyRecord.startDate:startWorkDate",
            "jobApplyRecord.resignDate:endWorkDate",
            "jobApplyRecord.receiver.realName:workUserRealName",
            "jobApplyRecord.receiver.token:workUserToken",
            "jobApplyRecord.receiver.mobile:workUserPhoneNumber",
            "jobApplyRecord.receiver.entrepreneurLevel:workUserEntrepreneurType",
            "jobApplyRecord.referral.realName:recommendWorkUserRealName",
            "jobApplyRecord.referral.token:recommendWorkUserToken",
            "jobApplyRecord.shop.id:storeId",
            "jobApplyRecord.shop.name:storeName",
            "jobApplyRecord.shop.logo.path:storeLogo",
            "jobApplyRecord.shop.province.name:storeProvinceName",
            "jobApplyRecord.shop.city.name:storeCityName",
            "jobApplyRecord.shop.area.name:storeAreaName",
            "registerSubLevel",
            "recommendSubLevel",
            "recommendRegisterUserRealName",
            "recommendRegisterUserToken"
    };

    private final static String[] JOBAPPLYRECORD_INFO = {
            "id",
            "job.reward:rewardAmount",
            "job.recType:jobType",
            "job.id:jobId",
            "job.name:position",
            "startDate:startWorkDate",
            "resignDate:endWorkDate",
            "receiver.realName:workUserRealName",
            "receiver.token:workUserToken",
            "referral.realName:recommendWorkUserRealName",
            "referral.token:recommendWorkUserToken",
            "shop.id:storeId",
            "shop.name:storeName",
            "shop.logo.path:storeLogo",
            "recState:workState"

    };

    private final static String[] JOBAPPLYRECORD_LIST = {
            "id",
            "job.reward:rewardAmount",
            "job.recType:jobType",
            "job.name:position",
            "startDate:startWorkDate",
            "resignDate:endWorkDate",
            "receiver.realName:workUserRealName",
            "referral.realName:recommendWorkUserRealName"
    };

    @Autowired
    private EntrepreneursService entrepreneursService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private MemberExtInfoService memberInfoService;
    @Autowired
    private AutomaticRenewalsService automaticRenewalsService;
    @Autowired
    private AccountFlowService accountFlowService;

    /**
     * 提交创业者申请
     */
    @RequestMapping(method = POST)
    public ResponseEntity save(String userToken, EntrepreneursApply apply) {
        entrepreneursService.saveApply(userToken, apply);
        return OK;
    }


    /**
     * 创业者状态
     */
    @RequestMapping(value = "/state", method = GET)
    public ResponseEntity getState(String userToken) {
        List<EntrepreneursApply> entrepreneursApplys = entrepreneursService.findApply(userToken);
        JSONObject jsonObjects = new JSONObject();
        if (entrepreneursApplys.isEmpty()) {
            jsonObjects.put("applyType", null);
            jsonObjects.put("applyState", null);
            jsonObjects.put("reviewDate", null);
            jsonObjects.put("payDate", null);
            jsonObjects.put("currentLevel", NONE);
            jsonObjects.put("totalIncome", null);
        } else {
            EntrepreneursApply entrepreneursApply = entrepreneursApplys.get(entrepreneursApplys.size() - 1);
            jsonObjects = propsFilter(entrepreneursApply, APPLY_INFO);
            jsonObjects.put("totalIncome", entrepreneursService.findIncome(userToken));
            if (isNotNull(entrepreneursApply))
                jsonObjects.put("currentLevel", entrepreneursApply.getMember().getEntrepreneurLevel());
            else jsonObjects.put("currentLevel", NONE);
            Entrepreneurs entrepreneurs = entrepreneursService.getEntrepreneurs(userToken);
            if (isNotNull(entrepreneurs))
                jsonObjects.put("payDate", entrepreneurs.getDate());
            else jsonObjects.put("payDate", null);
        }
        return ok(jsonObjects);
    }


    /**
     * 创业者付款
     */
    @RequestMapping(value = "/pay", method = POST)
    public ResponseEntity payForApply(String userToken, PayType payType, String openId) {
        Charge charge = entrepreneursService.payForApply(userToken, payType, openId);
        return payType == PAY_OFFLINE ? OK : ok(charge);
    }


    /**
     * 创业者详情
     */
    @RequestMapping(method = GET)
    public ResponseEntity detail(String userToken) {
        JSONObject jsonObject = new JSONObject();

        Entrepreneurs entrepreneurs = entrepreneursService.findEntrepreneurs(userToken);
        jsonObject.put("currentLevel", entrepreneurs.getMember().getEntrepreneurLevel());// 当前等级
        jsonObject.put("totalIncome", entrepreneurs.getIncome()); // 总收益
        jsonObject.put("entrepreneurRewardIncome", entrepreneurs.getEntrepreneurRewardIncome());// 创业奖励收益（一级创业者好友提成）

        entrepreneursService.entrepreneurCount(userToken, jsonObject);
        entrepreneursService.enAcount(userToken, jsonObject);

        return ok(jsonObject);
    }


    /**
     * 创业奖励收益明细
     */
    @RequestMapping(value = "/reward", method = GET)
    public ResponseEntity reward(String userToken, PageVO pageVO) {
        Page<AccountFlow> earningpage = accountFlowService.earningsList(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(earningpage, e -> {
            JSONObject jo = propsFilter(e, REWARD_LISTS);
            if (isNotNull(e.getCreateDate()))
                jo.put("date", e.getCreateDate());
            else jo.put("date", null);
            return jo;
        });
        return ok(jsonObject);
    }


    /**
     * 创业者收益明细列表
     */
    @RequestMapping(value = "/earningsList", method = GET)
    public ResponseEntity earningsList(String userToken, PageVO pageVO) {
        Page<AccountFlow> page = accountFlowService.findEarningsList(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(page, e -> propsFilter(e, EARNING_LISTS));
        return ok(jsonObject);
    }


    /**
     * 15.8创业者/合伙人 收益明细详情
     */
    @RequestMapping(value = "/earningsDetail", method = GET)
    public ResponseEntity earningsDetail(String userToken, Long id) {
        AccountFlow accountFlow = accountFlowService.findOne(id);
        ifNullThrow(accountFlow, TIP_NO_EARNING);
        JSONObject jsonObject = propsFilter(accountFlow, EARNING_INFOS);

        if (accountFlow.getJobApplyRecord() != null) {
            Member member = accountFlow.getJobApplyRecord().getReceiver();
            MemberExtInfo memberInfo = memberInfoService.findByMemberToken(member.getToken());
            SubLevelType registerSubLevel = memberRegRelService.regRelLevel(userToken, member.getToken());
            jsonObject.replace("registerSubLevel", isNotNull(registerSubLevel) ? registerSubLevel.getLevel() : 0);
            SubLevelType subLevelType = memberRecRelService.recRelLevel(userToken, member.getToken());
            jsonObject.replace("recommendSubLevel", isNotNull(subLevelType) ? subLevelType.getLevel() : 0);
            ifNotNullThen(memberInfo.getRecommendUser(), x -> {
                jsonObject.replace("recommendRegisterUserToken", memberInfo.getRecommendUser().getToken());
                jsonObject.replace("recommendRegisterUserRealName", memberInfo.getRecommendUser().getRealName());
            });
        }
        if (accountFlow.getSource() == ENTREPRENEUR_AWARD) {
            Member member = accountFlow.getEntrepreneursApply().getMember();
            jsonObject.replace("workUserRealName", member.getRealName());
            jsonObject.replace("workUserToken", member.getToken());
            jsonObject.replace("workUserPhoneNumber", member.getMobile());
            jsonObject.replace("workUserEntrepreneurType", member.getEntrepreneurLevel());
        }
        return ok(jsonObject);
    }


    /**
     * 15.9创业者管理推荐上班员工列表
     */
    @RequestMapping(value = "/jobApplyRecords", method = GET)
    public ResponseEntity jobApplyRecords(String userToken, WorkerQueryType type, PageVO pageVO) {
        Page<JobApplyRecord> jobApplyRecordPage = entrepreneursService.jobApplyRecords(userToken, type, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(jobApplyRecordPage, e -> propsFilter(e, JOBAPPLYRECORD_LIST));
        return ok(jsonObject);
    }

    //fixme  下个月的悬赏金的违约天数

    /**
     * 15.10创业者/合伙人 管理推荐上班员工详情
     */
    @RequestMapping(value = "/jARecordsDetail", method = GET)
    public ResponseEntity jARecordsDetail(String userToken, Long id) {
        JobApplyRecord jobApplyRecord = entrepreneursService.getDetail(userToken, id);
        JSONObject jsonObject = propsFilter(jobApplyRecord, JOBAPPLYRECORD_INFO);
        MemberExtInfo memberInfo = memberInfoService.findByMemberToken(jobApplyRecord.getReceiver().getToken());
        if (jobApplyRecord.getJob().getRecType() == ONCE) {
            jsonObject.put("paidRewardNumberOfMonth", 1);
        } else if (jobApplyRecord.getJob().getRecType() == MONTHLY) {
            AutomaticRenewals automaticRenewals = automaticRenewalsService.findAutoRenewals(jobApplyRecord.getJob().getId(),
                    jobApplyRecord.getReceiver().getId());
            ifNotNullThen(automaticRenewals, x -> {
                jsonObject.put("paidRewardNumberOfMonth", x.getTimes() - 1);
                Date now = new Date();
                if (x.getFinalDate() != null && now.getTime() > x.getFinalDate().getTime()) {
                    jsonObject.put("rewardAmountBreachDay", DateUtil.dayGap(x.getFinalDate(), now));
                } else {
                    jsonObject.put("rewardAmountBreachDay", null);
                }
            });
        } else {
            jsonObject.put("paidRewardNumberOfMonth", null);
        }
        SubLevelType subLevelType = memberRegRelService.regRelLevel(userToken, jobApplyRecord.getReceiver().getToken());
        jsonObject.put("registerSubLevel", isNotNull(subLevelType) ? subLevelType.getLevel() : 0);
        SubLevelType subLevelType1 = memberRecRelService.recRelLevel(userToken, jobApplyRecord.getReceiver().getToken());
        jsonObject.put("recommendSubLevel", isNotNull(subLevelType1) ? subLevelType1.getLevel() : 0);
        Member recommendUser = memberInfo.getRecommendUser();
        ifNotNullThen(recommendUser, x -> {
            jsonObject.put("recommendRegisterUserRealName", recommendUser.getRealName());
            jsonObject.put("recommendRegisterUserToken", recommendUser.getToken());
        });
        ifNullThen(recommendUser, () -> {
            jsonObject.put("recommendRegisterUserRealName", null);
            jsonObject.put("recommendRegisterUserToken", null);
        });
        return ok(jsonObject);
    }

}
