package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.model.EntrepreneursApply;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.EntrepreneursService;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.google.common.collect.ImmutableMap.of;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.IdentityType.NONE;
import static com.thousandsunny.service.ModuleKey.ApplyEnum;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;
import static com.thousandsunny.service.ModuleKey.EntrepreneursType;
import static com.thousandsunny.thirdparty.ModuleKey.SourceType;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(value = "/api/manager/entrepreneurs", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerEntrepreneursController {
    private static final String[] entrepreneursApply_list_json = {
            "id",
            "member.realName",
            "member.hpAccount",
            "type.title",
            "state",
            "payDate"
    };

    private static final String[] entrepreneursApply_json = {
            "name",
            "mobile",
            "type.title:applyType"
    };

    private static final String[] member_json = {
            "headImage.path:headImg",
            "id:vipId",
            "mobile",
            "realName:username",
            "username:nickName",
            "gender.title:gender",
            "hpAccount:HPaccount"
    };
    private static final String[] Entrepreneurs_Reward_Detail = {"id",
            "account.member.realName:countUser",
            "account.member.hpAccount:countHpAccount",
            "amount:countAmount",
            "source.title:countType",
            "entrepreneursApply.name:feeSource",
            "entrepreneursApply.member.hpAccount:feeSourceHpAccount",
            "entrepreneursApply.member.entrepreneurLevel.title:estpType",
            "createDate:countTime"};

    @Autowired
    private EntrepreneursService entrepreneursService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private AccountFlowService accountFlowService;

    /**
     * 创业者申请管理列表
     */
    @RequestMapping(value = "/entrepreneursApply", method = GET)
    public Result list(BackPageVo pageVo, String text, String tableType, Long province, Long city, Long area,
                       EntrepreneursType ESTPType, String auditStatus, String payStatus) {
        Page<EntrepreneursApply> applyPage = entrepreneursService.listEntrepreneursApply(pageVo.pageRequest(), decodePathVariable(text), tableType, province, city, area, ESTPType, auditStatus, payStatus);
        Page<JSONObject> jsonObject = applyPage.map(e -> {
            JSONObject jo = propsFilter(e, entrepreneursApply_list_json);
            jo.put("applyDate", ISO_DATETIME_FORMAT.format(e.getApplyDate()));
            final ApplyEnum state = e.getState();
            if (state == IN_REVIEW) {
                enumToJson(state, jo, "state");
            } else if (state == REVIEW_FAILED) {
                jo.put("state", new JSONObject(new HashedMap()) {{
                    put("text", "已拒绝");
                    put("key", "failed");
                }});
            } else if (state != ApplyEnum.NONE) {
                jo.put("state", new JSONObject(new HashedMap()) {{
                    put("text", "已通过");
                    put("key", "success");
                }});
            }
            jo.put("payStatus", new JSONObject(new HashedMap() {{
                if (state == SUCCESS) {
                    put("key", "pay");
                    put("text", "已支付");
                } else {
                    put("key", "noPay");
                    put("text", "未支付");
                }
            }}));
            if (e.getState() == SUCCESS) {
                AccountFlow accountFlow = accountFlowService.findEntrepreneursFlow(e.getId());
                ifNotNullThen(accountFlow, a -> jo.put("payDate", ISO_DATETIME_FORMAT.format(a.getCreateDate())));
            }
            return jo;
        });
        return OK(jsonObject);
    }

    /**
     * 审核
     */
    @RequestMapping(value = "/entrepreneursApply", method = PUT)
    public Result audit(Long id, String auditStatus, String reason) {
        entrepreneursService.audit(id, auditStatus, reason);
        return OK();
    }

    /**
     * 详情
     */
    @RequestMapping(value = "/entrepreneursApply/{id}", method = GET)
    public ResponseEntity<JSONObject> entrepreneursApplyInfo(@PathVariable Long id) {
        EntrepreneursApply entrepreneursApply = entrepreneursService.info(id);
        MemberExtInfo mInfo = memberExtInfoService.findByMemberToken(entrepreneursApply.getMember().getToken());

        JSONObject jsonObject = new JSONObject();
        JSONObject ESTPInfo = propsFilter(entrepreneursApply, entrepreneursApply_json);
        ESTPInfo.put("applyDate", ISO_DATETIME_FORMAT.format(entrepreneursApply.getApplyDate()));
        jsonObject.put("ESTPInfo", ESTPInfo);

        JSONObject credentialsInfo = new JSONObject();
        credentialsInfo.put("IDCardNo", entrepreneursApply.getIdCardNo());
        credentialsInfo.put("IDCardFrontImg", entrepreneursApply.getIdCardFront().getPath());
        credentialsInfo.put("IDCardHalfImg", entrepreneursApply.getHalf().getPath());
        jsonObject.put("credentialsInfo", credentialsInfo);

        JSONObject personalInfo = propsFilter(entrepreneursApply.getMember(), member_json);
        ifNotNullThen(entrepreneursApply.getMember().getUsername(), x -> personalInfo.replace("nickName", decodePathVariable(x)));
        if (entrepreneursApply.getMember().getBirthday() != null) {
            personalInfo.put("birthday", ISO_DATETIME_FORMAT.format(entrepreneursApply.getMember().getBirthday()));
        } else personalInfo.put("birthday", null);
        if (mInfo.getRecommendUser() != null) {
            if (mInfo.getRecommendUser().getRealName() != null) {
                personalInfo.put("referrer", mInfo.getRecommendUser().getRealName());
            } else personalInfo.put("referrer", null);
        } else personalInfo.put("referrer", null);
        if (isNotNull(mInfo.getRegisterTime()))
            personalInfo.put("regDate", ISO_DATETIME_FORMAT.format(mInfo.getRegisterTime()));
        else personalInfo.put("regDate", null);
        if (entrepreneursApply.getMember().getEntrepreneurLevel() != NONE)
            personalInfo.put("isEntrepreneurs", "是");
        else personalInfo.put("isEntrepreneurs", "否");
        if (entrepreneursApply.getMember().getPartnerLevel() != NO)
            personalInfo.put("isPartner", "是");
        else personalInfo.put("isPartner", "否");
        jsonObject.put("personalInfo", personalInfo);

        JSONObject auditStatus = new JSONObject();

        ImmutableMap<String, Object> map = of("key", entrepreneursApply.getState(), "text", entrepreneursApply.getState().getTitle());
        auditStatus.put("auditStatus", new JSONObject(map));
        auditStatus.put("reason", entrepreneursApply.getNotes());
        jsonObject.put("auditStatus", auditStatus);
        return ok(jsonObject);
    }

    /**
     * 9.3.1 创业费用奖励明细列表
     */
    @RequestMapping(value = "/rewards", method = GET)
    public Result entrepreneursRewardDetail(Date startTime, Date endTime, SourceType countType, String text, BackPageVo pageVo) {
        Page<AccountFlow> page = accountFlowService.periodSourceTypeAndKeyWord(startTime, endTime, countType, decodePathVariable(text), pageVo.pageRequest());
        return OK(page.map(a -> {
            JSONObject jsonObject = propsFilter(a, Entrepreneurs_Reward_Detail);
            jsonObject.put("countTime", ISO_DATETIME_FORMAT.format(a.getReceivedDate()));
            ifNullThen(a.getAccount().getMember(), () -> {
                jsonObject.replace("countUser", "平台");
                jsonObject.replace("countHpAccount", "-");
            });
            return jsonObject;
        }));

    }

}
