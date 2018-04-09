package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.portal.controller.dto.AreaDto;
import com.thousandsunny.service.model.PartnerApply;
import com.thousandsunny.service.service.AccountFlowService;
import com.thousandsunny.service.service.PartnerService;
import com.thousandsunny.thirdparty.model.AccountFlow;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.IdentityType.NONE;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.ApplyEnum;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/manager/partner", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerPartnerControllrer {


    private static final String[] partnerApply_list_json = {
            "id",
            "member.realName",
            "member.hpAccount",
            "state",
            "payDate",
            "applyArea:area"
    };


    private static final String[] partnerApply_json = {
            "name:username",
            "mobile",
    };

    private static final String[] member_json = {
            "headImage.path:headImg",
            "id:vipId",
            "mobile",
            "realName:username",
            "username:nickName",
            "gender.title:gender",
            "hpAccount:HPaccount",
    };
    private static final String[] area_json = {
            "id",
            "name"
    };


    @Autowired
    private PartnerService partnerService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private AccountFlowService accountFlowService;

    /**
     * 临时 - 合伙人线下付款确认成功
     */
    @RequestMapping(value = "/offLinePay", method = POST)
    public ResponseEntity confirm(String userToken, OperatorType operatorType) {
        partnerService.confirm(userToken, operatorType);
        return OK;
    }

    /**
     * 合伙人申请管理列表
     */
    @RequestMapping(value = "/partnerApply", method = GET)
    public Result list(String text, String tableType, Long province, Long city, Long area, String auditStatus, String payStatus, BackPageVo pageVo) {
        Page<PartnerApply> applyPage = partnerService.partnerApply(pageVo.pageRequest(), decodePathVariable(text), tableType, province, city, area, auditStatus, payStatus);
        Page<JSONObject> jsonObject = applyPage.map(e -> {
            JSONObject jo = propsFilter(e, partnerApply_list_json);
            jo.put("applyDate", ISO_DATETIME_FORMAT.format(e.getDate()));
            if (e.getState() == IN_REVIEW) {
                enumToJson(e.getState(), jo, "state");
            } else if (e.getState() == REVIEW_FAILED) {
                jo.put("state", new JSONObject(new HashedMap()) {{
                    put("text", "已拒绝");
                    put("key", "failed");
                }});
            } else if (e.getState() != ApplyEnum.NONE) {
                jo.put("state", new JSONObject(new HashedMap()) {{
                    put("text", "已通过");
                    put("key", "success");
                }});
            }
            jo.put("payStatus", new JSONObject(new HashedMap() {{
                if (e.getState() == SUCCESS) {
                    put("key", "pay");
                    put("text", "已支付");
                } else if (e.getState() == OFFLINE_PAY_CONFIRM) {
                    put("key", "line_paying");
                    put("text", "线下付款中");
                } else {
                    put("key", "noPay");
                    put("text", "未支付");
                }
            }}));
            if (e.getState() == SUCCESS) {
                AccountFlow accountFlow = accountFlowService.findPartnerFlow(e.getId());
                ifNotNullThen(accountFlow, a -> jo.put("payDate", ISO_DATETIME_FORMAT.format(accountFlow.getCreateDate())));
            }
            return jo;
        });
        return OK(jsonObject);

    }

    /**
     * 审核
     */
    @RequestMapping(value = "/partnerApply", method = PUT)
    public Result audit(Long id, String auditStatus, String reason) {
        partnerService.audit(id, auditStatus, reason);
        return OK();
    }

    /**
     * 详情
     */
    @RequestMapping(value = "/partnerApply/{id}", method = GET)
    public ResponseEntity partnerApplyInfo(@PathVariable Long id) {
        PartnerApply partnerApply = partnerService.info(id);
        MemberExtInfo mInfo = memberExtInfoService.findByMemberToken(partnerApply.getMember().getToken());

        JSONObject jsonObject = new JSONObject();
        JSONObject ESTPInfo = propsFilter(partnerApply, partnerApply_json);
        ESTPInfo.put("applyDate", isNotNull(partnerApply.getDate()) ? ISO_DATETIME_FORMAT.format(partnerApply.getDate()) : null);
        List<AreaDto> areaDtoList = new ArrayList<>();
        if (isNotNull(partnerApply.getProvince())) {
            AreaDto areaDto = new AreaDto();
            areaDto.setId(partnerApply.getProvince().getId());
            areaDto.setName(partnerApply.getProvinceName() + "-" + partnerApply.getCityName() + "-" + partnerApply.getAreaName());
            areaDtoList.add(areaDto);
        }
        if (isNotNull(partnerApply.getProvince2())) {
            AreaDto areaDto1 = new AreaDto();
            areaDto1.setId(partnerApply.getProvince2().getId());
            areaDto1.setName(partnerApply.getProvince2Name() + "-" + partnerApply.getCity2Name() + "-" + partnerApply.getArea2Name());
            areaDtoList.add(areaDto1);
        }
        List<JSONObject> area = simpleMap(areaDtoList, e -> propsFilter(e, area_json));
        ESTPInfo.put("area", area);

        jsonObject.put("ESTPInfo", ESTPInfo);

        JSONObject credentialsInfo = new JSONObject();
        credentialsInfo.put("IDCardNo", partnerApply.getIdCardNo());
        ifNotNullThen(partnerApply.getIdCard(), x -> {
            if (!x.getPath().equals(""))
                credentialsInfo.put("IDCardFrontImg", x.getPath());
            else credentialsInfo.put("IDCardFrontImg", null);
        });
        ifNotNullThen(partnerApply.getHalf(), x -> {
            if (!x.getPath().equals(""))
                credentialsInfo.put("IDCardHalfImg", x.getPath());
            else credentialsInfo.put("IDCardHalfImg", null);
        });
        jsonObject.put("credentialsInfo", credentialsInfo);

        JSONObject personalInfo = propsFilter(partnerApply.getMember(), member_json);
        ifNotBlankThen(partnerApply.getMember().getUsername(), name -> personalInfo.replace("nickName", decodePathVariable(name)));
        personalInfo.put("birthday", isNotNull(partnerApply.getMember().getBirthday()) ? ISO_DATE_FORMAT.format(partnerApply.getMember().getBirthday()) : null);
        if (isNotNull(mInfo.getRecommendUser())) {
            if (isNotBlank(mInfo.getRecommendUser().getRealName()))
                personalInfo.put("referrer", mInfo.getRecommendUser().getRealName());
            else personalInfo.put("referrer", "匿名用户");
        } else personalInfo.put("referrer", null);
        if (isNotNull(mInfo.getRegisterTime()))
            personalInfo.put("regDate", ISO_DATETIME_FORMAT.format(mInfo.getRegisterTime()));
        else personalInfo.put("regDate", null);

        if (partnerApply.getMember().getEntrepreneurLevel() != NONE)
            personalInfo.put("isEntrepreneurs", "是");
        else personalInfo.put("isEntrepreneurs", "否");
        if (partnerApply.getMember().getPartnerLevel() != NO)
            personalInfo.put("isPartner", "是");
        else personalInfo.put("isPartner", "否");
        jsonObject.put("personalInfo", personalInfo);

        JSONObject auditStatus = new JSONObject();
        auditStatus.put("auditStatus", new JSONObject(new HashedMap() {{
            put("key", partnerApply.getState());
            put("text", partnerApply.getState().getTitle());
        }}));
        auditStatus.put("reason", partnerApply.getNotes());
        jsonObject.put("auditStatus", auditStatus);
        return ok(jsonObject);
    }

}
