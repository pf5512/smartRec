package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.portal.controller.dto.RewardHelp;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.*;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.IdentityType.NONE;
import static com.thousandsunny.service.ModuleKey.KeyPercentage.*;
import static com.thousandsunny.service.ModuleKey.RecState;
import static com.thousandsunny.service.ModuleTips.TIP_PARAM_FALSE;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/manager/shops", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerShopController {
    @Autowired
    private ShopService shopService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobApplyRecordService jobApplyRecordService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private MemberRecRelService memberRecRelService;
    @Autowired
    private MemberRegRelService memberRegRelService;
    @Autowired
    private ShopOperateService shopOperateService;

    private static final String[] SHOPS_JSON = {
            "id",
            "name",
            "owner.realName",
            "owner.hpAccount"
    };
    private static final String[] SHOP_JSON = {
            "id:shopId",
            "name:shopName",
            "owner.realName:contacter",
            "owner.mobile:tel",
            "owner.hpAccount:hpAccount",
            "ownerPosition:positon",
            "address",
            "shopArea"
    };
    private static final String[] OPRATION_JSON = {
            "name:text",
            "code:value"
    };

    private static final String[] MEMBER_JSON = {
            "headImage.path:img",
            "id:memberId",
            "mobile:tel",
            "realName:userName",
            "username:nickName",
            "gender.title:sex",
            "hpAccount",
            "recommender",
            "birthday"
    };

    private static final String[] JOBS_JSON = {
            "id",
            "name",
            "recType.title",
            "reward",
            "createTime",
            "isEnableBoolean:isEnable",
            "state"
    };

    private static final String[] JOB_JSON = {
            "id:jobID",
            "shop.name:shopName",
            "name:jobName",
            "workExperience",
            "reward",
            "state.title:jobStatus",
            "salary.name:salary",
            "period.name:workExperience",
    };

    private static final String[] JOBAPPLY_JSON = {
            "id",
            "receiver.realName:name",
            "receiver.hpAccount:hpAccount",
            "referral.realName:jobReferrer",
            "startDate:entryTime",
            "recState.title:workStatus",
            "refund.title:depositStatus",
            "reward.title:rewardStatus"
    };

    private static final String[] REWARD_JSON = {
            "relationshipType",
            "name",
            "hpAccount",
            "reward"
    };

    private static final String[] JOB_OPRATION_JSON = {
            "id",
            "source.title:operateType",
            "job.reward:jobreward",
            "job.epmCount:num",
            "amount:allMoney",
            "payType.title:payType",
            "orderNo:payFlowno"
    };


    /**
     * 店铺列表
     */
    @RequestMapping(value = "/shop", method = GET)
    public Result list(BackPageVo pageVO, String text, Long province, Long city, Long area, String partnerStatus, String transferStatus) {
        Page<Shop> shopPage = shopService.listShops(pageVO.pageRequest(), decodePathVariable(text), province, city, area, partnerStatus, transferStatus);
        Page<JSONObject> jsonObject = shopPage.map(e -> {
            JSONObject jo = propsFilter(e, SHOPS_JSON);
            if (isNotNull(e.getDate())) jo.put("createTime", ISO_DATETIME_FORMAT.format(e.getDate()));
            else jo.put("createTime", null);
            jo.put("state", e.getState() != NO);

            JSONObject findHelp = new JSONObject();
            findHelp.put("text", null);
            findHelp.put("key", null);
            if (isNotNull(e.getFindHelp())) {
                findHelp.put("text", e.getFindHelp().getName());
                findHelp.put("key", e.getFindHelp().getCode());
            }

            JSONObject isTransfer = new JSONObject();
            isTransfer.put("text", null);
            isTransfer.put("key", null);
            ifNotNullThen(e.getIsTransfer(), i -> {
                isTransfer.put("text", e.getIsTransfer().getName());
                isTransfer.put("key", e.getIsTransfer().getCode());
            });
            jo.put("findHelp", findHelp);
            jo.put("isTransfer", isTransfer);
            return jo;
        });
        return OK(jsonObject);

    }


    /**
     * 删除
     */
    @RequestMapping(value = "/shop", method = DELETE)
    public Result videoDelete(String id) {
        String[] strings = id.split(",");
        stream(strings).map(Long::parseLong).forEach(shopService::shopDelete);
        return OK();
    }


    /**
     * 启用
     */
    @RequestMapping(value = "/enabled", method = POST)
    public Result enabledComment(Long id) {
        shopService.enabled(id);
        return OK();
    }

    /**
     * 详情
     */
    @RequestMapping(value = "/shop/{id}", method = GET)
    public ResponseEntity info(@PathVariable Long id) {
        String[] SHOP_COOPERATION_INFO = {"shopSquare:area", "shopRental:rent", "remark:explanation",};
        Shop shop = shopService.findOne(id);
        JSONObject jsonObject = new JSONObject();
        JSONObject basicInformation = propsFilter(shop, SHOP_JSON);
        basicInformation.put("positon", shop.getOwnerPosition().getTitle());
        ifNotNullThen(shop.getDate(), x -> basicInformation.put("date", ISO_DATE_FORMAT.format(x)));
        basicInformation.put("shopCoordinate", shop.getLongitude() + "," + shop.getLatitude());
        JSONObject coverImg = new JSONObject();
        if (isNotNull(shop.getLogo())) {
            coverImg.put("url", shop.getLogo().getPath());
            coverImg.put("description", shop.getLogo().getTitle());
        } else {
            coverImg.put("url", null);
            coverImg.put("description", null);
        }
        basicInformation.put("coverImg", coverImg);

        StringBuffer shopArea = new StringBuffer();
        ifNotNullThen(shop.getProvince(), t -> shopArea.append(t.getName()));
        ifNotNullThen(shop.getCity(), t -> shopArea.append("-").append(t.getName()));
        ifNotNullThen(shop.getArea(), t -> shopArea.append("-").append(t.getName()));

        basicInformation.replace("shopArea", shopArea.toString());
        jsonObject.put("basicInformation", basicInformation);
        String[] brightSpots = shop.getBrightSpots().split(",");
        List<JSONObject> jsonObjects = new ArrayList<>();
        for (int i = 0; i < brightSpots.length; i++) {
            JSONObject jo = new JSONObject();
            jo.put("text", brightSpots[i]);
            jo.put("value", i + 1);
            jsonObjects.add(jo);
        }
        jsonObject.put("Sterns", jsonObjects);
        List<JSONObject> imgUrls = simpleMap(shop.getPhotos(), e -> {
            JSONObject img = new JSONObject();
            img.put("url", e.getPath());
            if (isNotNull(e.getTitle())) {
                img.put("description", e.getTitle());
            } else img.put("description", null);
            return img;
        });
        jsonObject.put("imgUrls", imgUrls);
        String logo = shop.getLogo().getPath();
        JSONObject json = new JSONObject();
        json.put("url", logo);
        List<JSONObject> jsonLogs = new ArrayList<JSONObject>();
        jsonLogs.add(json);
        jsonObject.put("logo", jsonLogs);

        JSONObject userInformation = propsFilter(shop.getOwner(), MEMBER_JSON);
        ifNotNullThen(shop.getOwner().getUsername(), x -> userInformation.replace("nickName", decodePathVariable(x)));
        ifNotNullThen(shop.getOwner().getBirthday(), x -> userInformation.replace("birthday", ISO_DATE_FORMAT.format(x)));
        MemberExtInfo mInfo = memberExtInfoService.findByMemberToken(shop.getOwner().getToken());
        ifNotNullThen(mInfo.getRecommendUser(), t -> userInformation.replace("recommender", t.getRealName()));
        userInformation.put("regDate", ISO_DATE_FORMAT.format(mInfo.getRegisterTime()));
        if (shop.getOwner().getEntrepreneurLevel() == NONE)
            userInformation.put("isChuangyeer", "否");
        else userInformation.put("isChuangyeer", "是");
        if (shop.getOwner().getPartnerLevel() == NO)
            userInformation.put("isHehuo", "否");
        else userInformation.put("isHehuo", "是");
        jsonObject.put("userInformation", userInformation);

        ShopOperate shopOperate = shopOperateService.findShopCooperationInfo(shop);
        if (isNotNull(shopOperate)) {
            JSONObject cooperation = propsFilter(shopOperate, SHOP_COOPERATION_INFO);
            if (isNotNull(shopOperate.getLaunchTime()))
                cooperation.put("publishTime", ISO_DATETIME_FORMAT.format(shopOperate.getLaunchTime()));
            else cooperation.put("publishTime", null);
            if (isNotNull(shopOperate.getCloseTime()))
                cooperation.put("closeTime", ISO_DATETIME_FORMAT.format(shopOperate.getCloseTime()));
            else cooperation.put("closeTime", null);
            jsonObject.put("cooperation", cooperation);
        } else jsonObject.put("cooperation", null);
        ShopOperate shopOperate1 = shopOperateService.findShopTransferInfo(shop);
        if (isNotNull(shopOperate1)) {
            JSONObject transfer = propsFilter(shopOperate1, SHOP_COOPERATION_INFO);
            if (isNotNull(shopOperate1.getLaunchTime()))
                transfer.put("publishTime", ISO_DATETIME_FORMAT.format(shopOperate.getLaunchTime()));
            else transfer.put("publishTime", null);
            if (isNotNull(shopOperate1.getCloseTime()))
                transfer.put("closeTime", ISO_DATETIME_FORMAT.format(shopOperate.getCloseTime()));
            else transfer.put("closeTime", null);
            jsonObject.put("transfer", transfer);
        } else jsonObject.put("transfer", null);
        return ok(jsonObject);
    }


    /**
     * 店铺岗位列表
     */
    @RequestMapping(value = "/job", method = GET)
    public Result listJob(BackPageVo pageVO, String text, String recruitmentType, String positionStatus, Long shopId) {
        Page<Job> jobPage = jobService.listJobs(pageVO.pageRequest(), decodePathVariable(text), recruitmentType, positionStatus, shopId);

        Page<JSONObject> jsonObject = jobPage.map(j -> {
            JSONObject jo = propsFilter(j, JOBS_JSON);
            ifNotNullThen(j.getDate(), t -> jo.replace("createTime", ISO_DATETIME_FORMAT.format(j.getDate())));
            enumToJson(j.getState(), jo, "state");
            return jo;
        });
        return Result.OK(jsonObject);
    }


    /**
     * 店铺岗位启用
     */
    @RequestMapping(value = "/job", method = PUT)
    public Result enabledJob(Long id) {
        jobService.enabled(id);
        return OK();
    }

    /**
     * 店铺岗位详情
     */
    @RequestMapping(value = "/job/{id}", method = GET)
    public ResponseEntity jobInfo(@PathVariable Long id) {
        Job job = jobService.findOne(id);
        JSONObject jsonObject = propsFilter(job, JOB_JSON);
        jsonObject.put("time", ISO_DATE_FORMAT.format(job.getDate()));
//        jsonObject.put("salary", job.getSalary().getMinVal() + "-" + job.getSalary().getMaxVal());
//        jsonObject.put("workExperience", job.getPeriod().getMinVal() + "-" + job.getPeriod().getMaxVal());
        jsonObject.put("jobDescribe", job.getDescription().split(","));

        Map<String, Object> map = new HashMap<>();
        map.put("value", job.getRecType());
        map.put("text", job.getRecType().getTitle());
        jsonObject.put("recruitmentType", new JSONObject(map));
        return ok(jsonObject);
    }


    /**
     * 店铺岗位操作列表
     */
    @RequestMapping(value = "/jobOpration", method = GET)
    public Result oprationList(BackPageVo pageVO, Long jobId) {
        Page<AccountFlow> jobPage = jobService.listJobOptation(pageVO.pageRequest(), Long.valueOf(jobId));
        Page<JSONObject> jsonObject = jobPage.map(e -> {
            JSONObject jo = propsFilter(e, JOB_OPRATION_JSON);
            jo.put("time", ISO_DATETIME_FORMAT.format(e.getCreateDate()));
            jo.put("payTime", ISO_DATETIME_FORMAT.format(e.getUpdateDate()));
            return jo;
        });
        return Result.OK(jsonObject);
    }


    /**
     * 管理上班列表
     */
    @RequestMapping(value = "/jobApplyRecords", method = GET)
    public Result manageWork(BackPageVo pageVO, String text, RecState workStatus, Long positionId) {
        ifNullThrow(positionId, TIP_PARAM_FALSE);
        Page<JobApplyRecord> jobPage = jobApplyRecordService.list(pageVO.pageRequest(), decodePathVariable(text), workStatus, positionId);
        Page<JSONObject> jsonObject = jobPage.map(e -> {
            JSONObject jo = propsFilter(e, JOBAPPLY_JSON);
            jo.put("dimissionTime", isNotNull(e.getResignDate()) ? ISO_DATETIME_FORMAT.format(e.getResignDate()) : null);
            MemberExtInfo mInfo = memberExtInfoService.findByMemberToken(e.getReceiver().getToken());
            if (isNotNull(mInfo)) {
                if (isNotNull(mInfo.getRecommendUser()))
                    jo.put("regReferrer", mInfo.getRecommendUser().getRealName());
            } else
                jo.put("regReferrer", null);
            return jo;
        });
        return Result.OK(jsonObject);
    }


    /**
     * 奖惩分成详情
     */
    @RequestMapping(value = "/reward", method = GET)
    public Result reward(Long id) {
        JobApplyRecord jobApplyRecord = jobApplyRecordService.findJobApplyRecord(id);
        Member receiver = jobApplyRecord.getReceiver();

        BenefitRel benefitRel = jobApplyRecordService.findBenefitRel(receiver.getToken(), jobApplyRecord.getJob().getId());
        List<RewardHelp> rewardHelps = new ArrayList<>();
        BigDecimal rewardMoney = jobApplyRecord.getJob().getReward();
        BigDecimal platWard = rewardMoney;
        if (isNotNull(benefitRel)) {
            RewardHelp rewardHelp = getRewardHelp(CONSTANT_CAR_FEE.val(), receiver.getId(), CONSTANT_CAR_FEE.getTitle());
            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, CONSTANT_CAR_FEE.val());
        }

        Partner partner = partnerService.findPartner(jobApplyRecord.getJob().getShop().getArea());
        if (isNotNull(partner)) {
            RewardHelp rewardHelp = getRewardHelp(rewardMoney.multiply(CONSTANT_PARTNER.val()), partner.getMember().getId(),
                    CONSTANT_PARTNER.getTitle());

            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, rewardMoney.multiply(CONSTANT_PARTNER.val()));
        }

        Map<String, Long> idsRec = memberRecRelService.recCascade(receiver.getId());
        Long recId1 = idsRec.get("regId1");
        if (isNotNull(recId1)) {
            RewardHelp rewardHelp = getRewardHelp(rewardMoney.multiply(CONSTANT_REC_FRIEND.val()), recId1, CONSTANT_REC_FRIEND.getTitle());
            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, rewardMoney.multiply(CONSTANT_REC_FRIEND.val()));
        }

        Long recId2 = idsRec.get("id2");
        if (isNotNull(recId2)) {
            RewardHelp rewardHelp = getRewardHelp(rewardMoney.multiply(CONSTANT_REC_ACQUAINTANCE.val()), recId2, CONSTANT_REC_ACQUAINTANCE.getTitle());
            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, rewardMoney.multiply(CONSTANT_REC_ACQUAINTANCE.val()));
        }

        Long recId3 = idsRec.get("id3");
        if (isNotNull(recId3)) {
            RewardHelp rewardHelp = getRewardHelp(rewardMoney.multiply(CONSTANT_REC_CONTACTS.val()), recId3, CONSTANT_REC_CONTACTS.getTitle());
            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, rewardMoney.multiply(CONSTANT_REC_CONTACTS.val()));
        }

        Map<String, Long> idsReg = memberRegRelService.regCascade(receiver.getId());
        Long regId1 = idsReg.get("regId1");
        if (isNotNull(regId1)) {
            RewardHelp rewardHelp = getRewardHelp(rewardMoney.multiply(CONSTANT_REG_FRIEND.val()), regId1, CONSTANT_REG_FRIEND.getTitle());
            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, rewardMoney.multiply(CONSTANT_REG_FRIEND.val()));
        }

        Long regId2 = idsReg.get("id2");
        if (isNotNull(regId2)) {
            RewardHelp rewardHelp = getRewardHelp(rewardMoney.multiply(CONSTANT_REG_ACQUAINTANCE.val()), regId2, CONSTANT_REG_ACQUAINTANCE.getTitle());
            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, rewardMoney.multiply(CONSTANT_REG_ACQUAINTANCE.val()));
        }

        Long regId3 = idsReg.get("id3");
        if (isNotNull(regId3)) {
            RewardHelp rewardHelp = getRewardHelp(rewardMoney.multiply(CONSTANT_REG_CONTACTS.val()), regId3, CONSTANT_REG_CONTACTS.getTitle());
            rewardHelps.add(rewardHelp);
            platWard = getSubtract(platWard, rewardMoney.multiply(CONSTANT_REG_CONTACTS.val()));
        }

        JSONObject jsonObject = new JSONObject();
        List<JSONObject> jos = simpleMap(rewardHelps, e -> propsFilter(e, REWARD_JSON));
        jsonObject.put("totalWard", jobApplyRecord.getJob().getReward());
        jsonObject.put("platWard", platWard);
        jsonObject.put("rewardDetail", listToJson(jos));

        return OK(jsonObject);
    }

    private BigDecimal getSubtract(BigDecimal platWard, BigDecimal multiply) {
        return platWard.subtract(multiply);
    }

    private RewardHelp getRewardHelp(BigDecimal rewardMoney, Long memberId, String type) {
        Member member = memberService.findOne(memberId);
        RewardHelp rewardHelp = new RewardHelp();
        rewardHelp.setRelationshipType(type);
        rewardHelp.setName(member.getRealName());
        rewardHelp.setHpAccount(member.getHpAccount());
        rewardHelp.setReward(rewardMoney);
        return rewardHelp;
    }


}
