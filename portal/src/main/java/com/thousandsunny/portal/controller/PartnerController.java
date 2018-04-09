package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.pingplusplus.model.Charge;
import com.thousandsunny.common.DateUtil;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey.SubLevelType;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.IdentityType.NONE;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;
import static com.thousandsunny.service.ModuleKey.RecState.*;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.MONTHLY;
import static com.thousandsunny.service.ModuleKey.RecruitmentType.ONCE;
import static com.thousandsunny.service.ModuleKey.WorkerQueryType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api/portal/partner", produces = APPLICATION_JSON_UTF8_VALUE)
public class PartnerController {
    private String[] partner_detail_json = {
            "state:applyState",
            "provinceName:applyedArea1ProvinceName",
            "cityName:applyedArea1CityName",
            "areaName:applyedArea1AreaName",
            "province2Name:applyedArea2ProvinceName",
            "city2Name:applyedArea2CityName",
            "area2Name:applyedArea2AreaName",
            "notes:reviewRemark"
    };

    private String[] partner_area_json = {
            "area.id:areaId",
            "area.name:areaName"
    };

    private String[] partner_earning_json = {
            "id",
            "earningType:type",
            "date",
            "jobApplyRecord.job.reward:rewardAmount",
            "jobApplyRecord.job.recType:jobType",
            "amount",
    };

    private String[] worker_query_json = {
            "id",
            "job.reward:rewardAmount",
            "job.recType:jobType",
            "job.name:position",
            "startDate:startWorkDate",
            "resignDate:endWorkDate",
            "receiver.realName:workUserRealName",
            "referral.realName:recommendWorkUserRealName",
    };

    private String[] shop_json = {
            "id",
            "name",
            "logo.path:logo"
    };

    private String[] job_json = {
            "id",
            "name",
            "date",
            "shop.name:storeName",
            "shop.logo.path:storeLogoImageUrl",
            "salary.name:salary",
            "period.name:workYear",
            "epmCount:unFindPeopleCount",
            "reward:rewardAmount",
            "recType:jobType",
            "shop.area.name:areaName"

    };

    private String[] JOBAPPLYRECORD_INFO = {
            "id",
            "job.reward:rewardAmount",
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
            "shop.province.name:storeProvinceName",
            "shop.city.name:storeCityName",
            "shop.area.name:storeAreaName",
            "recState:workState"
    };

    private final static String[] EARNING_LISTS = {
            "id",
            "source:type",
            "createDate:date",
            "amount",
            "jobApplyRecord.job.recType:jobType",
            "jobApplyRecord.job.reward:rewardAmount"
    };


    @Autowired
    private PartnerService partnerService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private JobService jobService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberExtInfoService memberInfoService;
    @Autowired
    private AutomaticRenewalsService automaticRenewalsService;
    @Autowired
    private AccountFlowService accountFlowService;

    /**
     * 剩余的可选择列表
     */
    @RequestMapping(value = "/region/all", method = GET)
    public ResponseEntity cascadeRegion() {
        return ok(listToJson(partnerService.avaCascadeAllRegions()));
    }

    /**
     * 提交合伙人申请
     */
    @RequestMapping(method = POST)
    public ResponseEntity apply(String userToken, PartnerApply partnerApply) {
        partnerService.apply(userToken, partnerApply);
        return OK;
    }

    /**
     * 合伙人状态
     */
    @RequestMapping(value = "/state", method = GET)
    public ResponseEntity state(String userToken) {
        List<PartnerApply> partnerApplys = partnerService.getState(userToken);
        JSONObject jsonObject = new JSONObject();
        int applyingAreaCount = 0;
//        boolean flag = true;
//        for (PartnerApply ptApply : partnerApplys) {
//            if (ptApply.getState() == IN_REVIEW || ptApply.getState() == REVIEW_SUCCESS
//                    || ptApply.getState() == OFFLINE_PAY_CONFIRM || ptApply.getState() == SUCCESS) {
//                flag = false;
//                break;
//            }
//        }
        if (partnerApplys.isEmpty()) { //不是合伙人时返回：
            jsonObject.put("applyState", NONE);
            jsonObject.put("applyedArea1ProvinceName", null);
            jsonObject.put("applyedArea1CityName", null);
            jsonObject.put("applyedArea1AreaName", null);
            jsonObject.put("applyedArea2ProvinceName", null);
            jsonObject.put("applyedArea2CityName", null);
            jsonObject.put("applyedArea2AreaName", null);
            jsonObject.put("totalIncome", 0);
            jsonObject.put("applyingAreaCount", 0);
        } else {
            int n = 1;
            for (PartnerApply ptApply : partnerApplys) {
                if (ptApply.getState() == IN_REVIEW || ptApply.getState() == REVIEW_SUCCESS
                        || ptApply.getState() == OFFLINE_PAY_CONFIRM || ptApply.getState() == SUCCESS) {
                    n = applyedAreaInfo(jsonObject, n, ptApply);
                }
            }
            PartnerApply latestPtApply = partnerApplys.get(partnerApplys.size() - 1);
            if (latestPtApply.getState() == REVIEW_FAILED)
                n = applyedAreaInfo(jsonObject, n, latestPtApply);
            if (latestPtApply.getArea() != null) applyingAreaCount++;
            if (latestPtApply.getArea2() != null) applyingAreaCount++;
            jsonObject.put("applyState", latestPtApply.getState());
            jsonObject.put("reviewRemark", latestPtApply.getNotes());
            jsonObject.put("applyingAreaCount", applyingAreaCount);
        }
        return ok(jsonObject);
    }

    private int applyedAreaInfo(JSONObject jsonObject, int n, PartnerApply latestPtApply) {
        if (isNotNull(latestPtApply.getArea())) {
            jsonObject.put("applyedArea" + n + "ProvinceName", latestPtApply.getProvince().getName());
            jsonObject.put("applyedArea" + n + "CityName", latestPtApply.getCity().getName());
            jsonObject.put("applyedArea" + n + "AreaName", latestPtApply.getArea().getName());
            Partner partner = partnerService.findByMemberIdAndAreaId(latestPtApply.getMember().getId(), latestPtApply.getArea().getId());
            if (isNotNull(partner))
                jsonObject.put("area" + n + "TotalIncome", partner.getIncome());
            else jsonObject.put("area" + n + "TotalIncome", 0);
            n++;
        }
        if (isNotNull(latestPtApply.getArea2())) {
            jsonObject.put("applyedArea" + n + "ProvinceName", latestPtApply.getProvince2().getName());
            jsonObject.put("applyedArea" + n + "CityName", latestPtApply.getCity2().getName());
            jsonObject.put("applyedArea" + n + "AreaName", latestPtApply.getArea2().getName());
            Partner partner = partnerService.findByMemberIdAndAreaId(latestPtApply.getMember().getId(), latestPtApply.getArea2().getId());
            if (isNotNull(partner))
                jsonObject.put("area" + n + "TotalIncome", partner.getIncome());
            else jsonObject.put("area" + n + "TotalIncome", 0);
            n++;
        }
        return n;
    }

    /**
     * 合伙人付款
     */
    @RequestMapping(value = "/pay", method = POST)
    public ResponseEntity payForApply(String userToken, PayType payType, String openId) {
        Charge charge = partnerService.payForApply(userToken, payType, openId);
        return ok(isNotNull(charge) ? charge : newHashMap());
    }

    /**
     * 合伙人详情
     */
    @RequestMapping(method = GET)
    public ResponseEntity detail(String userToken) {
        List<Partner> partners = partnerService.getPartner(userToken);
        JSONObject jsonObject = new JSONObject();
        List<JSONObject> jos = simpleMap(partners, e -> {
            JSONObject j = propsFilter(e, partner_area_json);
            j.put("totalIncome", e.getIncome());
            j.put("storeCount", shopService.getShopNumber(e.getArea().getId()));
            j.put("oncePeopleCount", jobService.getRecruitingNumber(ONCE, e.getArea().getId()));
            j.put("monthlyPeopleCount", jobService.getRecruitingNumber(MONTHLY, e.getArea().getId()));
            j.put("onceRewardLessThanOneMonthPeopleCount", jobService.getInJobWorkerNumber(ONCE, WORKING, e.getArea().getId(), false));
            j.put("onceRewardMoreThanOneMonthPeopleCount", jobService.getInJobWorkerNumber(ONCE, WORKING, e.getArea().getId(), true));
            j.put("onceRewardResignedPeopleCount", jobService.getQuitWorkerNumber(ONCE, ALREADY_RESIGN, e.getArea().getId()));
            j.put("onceRewardFailedPeopleCount", jobService.getQuitWorkerNumber(ONCE, WORK_FAIL, e.getArea().getId()));
            j.put("monthlyRewardLessThanOneMonthPeopleCount", jobService.getInJobWorkerNumber(MONTHLY, WORKING, e.getArea().getId(), false));
            j.put("monthlyRewardMoreThanOneMonthPeopleCount", jobService.getInJobWorkerNumber(MONTHLY, WORKING, e.getArea().getId(), true));
            j.put("monthlyRewardResignedPeopleCount", jobService.getQuitWorkerNumber(MONTHLY, ALREADY_RESIGN, e.getArea().getId()));
            j.put("monthlyRewardFailedPeopleCount", jobService.getQuitWorkerNumber(MONTHLY, WORK_FAIL, e.getArea().getId()));
            return j;
        });
        jsonObject.put("areaList", jos);
        return ok(jsonObject);
    }

    /**
     * 合伙人收益明细列表
     */
    @RequestMapping(value = "/earnings", method = GET)
    public ResponseEntity partnerEarning(String userToken, PageVO pageVO) {
        Page<AccountFlow> partnerEarning = accountFlowService.partnerEarningsList(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(partnerEarning, e -> propsFilter(e, EARNING_LISTS));
        return ok(jsonObject);
    }

    /**
     * 合伙人管理推荐上班员工列表
     */
    @RequestMapping(value = "/workers", method = GET)
    public ResponseEntity workers(String userToken, Long areaId, WorkerQueryType type, PageVO pageVO) {
        List<JobApplyRecord> applyRecords = partnerService.queryWorkers(userToken, areaId, type, pageVO.pageRequest());
        List<JobApplyRecord> jobApplyRecordList = newArrayList();
        int num = pageVO.getPageNo();
        int size = pageVO.getPageSize();
        int startIndex = num * size;
        int endIndex = (num + 1) * size - 1;
        if ((num + 1) * size >= applyRecords.size()) {
            endIndex = applyRecords.size() - 1;
        }
        for (int i = startIndex; i <= endIndex; i++) {
            jobApplyRecordList.add(applyRecords.get(i));
        }
        List<JSONObject> jsonObjects = simpleMap(jobApplyRecordList, x -> propsFilter(x, worker_query_json));
        JSONObject jsonObject = listToJson(jsonObjects);
        if ((num + 1) * size < applyRecords.size()) {
            jsonObject.put("last", false);
        } else {
            jsonObject.put("last", true);
        }
        if (num == 0) {
            jsonObject.put("first", true);
        } else {
            jsonObject.put("first", false);
        }
        return ok(jsonObject);

    }

    /**
     * 合伙区域店铺
     */
    @RequestMapping(value = "/shopList", method = GET)
    public ResponseEntity shopList(String userToken, PageVO pageVO, Long areaId) {
        Page<Shop> shops = partnerService.shopList(userToken, pageVO.pageRequest(), areaId);
        JSONObject jsonObject = pageToJson(shops, e -> {
            JSONObject jb = propsFilter(e, shop_json);
            String address = "";
            if (isNotNull(e.getProvince())) address += e.getProvince().getName();
            if (isNotNull(e.getCity())) address += e.getCity().getName();
            if (isNotNull(e.getArea())) address += e.getArea().getName();
            address += e.getAddress();
            jb.put("address", address);
            return jb;
        });
        return ok(jsonObject);

    }


    /**
     * 合伙区域岗位列表
     */
    @RequestMapping(value = "/jobList", method = GET)
    public ResponseEntity jobList(String userToken, ModuleKey.RecruitmentType type, PageVO pageVO, Long areaId) {
        Page<Job> jobPage = jobService.jobList(userToken, type, pageVO.pageRequest(), areaId);
        JSONObject jsonObject = pageToJson(jobPage, e -> {
            JSONObject jb = propsFilter(e, job_json);
            return jb;
        });
        return ok(jsonObject);

    }

    /**
     * 合伙人管理推荐上班员工详情
     *
     * @Author xiao xue wei
     * @Date 2016/12/9
     */
    @RequestMapping(value = "/partnerWorkList", method = GET)
    public ResponseEntity partnerWorkList(String userToken, Long id) {
        JobApplyRecord jobApplyRecord = partnerService.getDetail(userToken, id);
        JSONObject jsonObject = propsFilter(jobApplyRecord, JOBAPPLYRECORD_INFO);
        jsonObject.put("jobType", jobApplyRecord.getJob().getRecType().name());
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
        jsonObject.put("recommendSubLevel", memberRecRelService.recRelLevel(userToken, jobApplyRecord.getReceiver().getToken()));
        if (memberInfo.getRecommendUser() != null) {
            jsonObject.put("recommendRegisterUserRealName", memberInfo.getRecommendUser().getRealName());
            jsonObject.put("recommendRegisterUserToken", memberInfo.getRecommendUser().getToken());
        } else {
            jsonObject.put("recommendRegisterUserRealName", null);
            jsonObject.put("recommendRegisterUserToken", null);
        }
        return ok(jsonObject);
    }
}
