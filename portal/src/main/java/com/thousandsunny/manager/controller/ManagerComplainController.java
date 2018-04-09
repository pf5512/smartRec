package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey.ComplainType;
import com.thousandsunny.service.model.Complain;
import com.thousandsunny.service.model.Partner;
import com.thousandsunny.service.model.School;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.repository.PartnerRepository;
import com.thousandsunny.service.service.ComplainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.ComplainType.*;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2016/11/22.
 */
@RestController
@RequestMapping(value = "/api/manager/complain", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerComplainController {
    @Autowired
    private ComplainService complainService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private PartnerRepository partnerRepository;
    private static final String[] JSON = new String[]{"id", "complainant.realName", "complainant.hpAccount", "type", "date", "isDeal"};

    /**
     * 12.2.1 投诉列表
     *
     * @Author mu.jie
     * @Date 2016/11/22
     */
    @RequestMapping(method = GET)
    public Result findComplainList(BackPageVo backPageVo, String text, BooleanEnum tableType, ComplainType complainType) {
        Page<Complain> page = complainService.findComplainList(backPageVo, decodePathVariable(text), tableType, complainType);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, JSON);
            if (x.getType() == COMPLAINT_TO_SCHOOL)
                ifNotNullThen(x.getSchool(), school -> ifNotNullThen(school.getMember(), member -> {
                    if (isNotNull(member.getRealName()))
                        jo.put("defendantName", x.getSchool().getMember().getRealName());
                    else jo.put("defendantName", member.getMobile());
                }));
            else if (x.getType() == USER_COMPLAINT_TO_STORE_UNCONFIRM_WORK || x.getType() == COMPLAINT_TO_STORE)
                jo.put("defendantName", x.getShop().getOwner().getRealName());
            else jo.put("defendantName", x.getDefendant().getRealName());
            ifNotNullThen(x.getType(), type -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("text", x.getType().getTitle());
                jsonObject.put("key", x.getType());
                jo.replace("type", jsonObject);
            });
            ifNotNullThen(x.getIsDeal(), isDeal -> {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("key", x.getIsDeal());
                jsonObject1.put("text", x.getIsDeal() == YES ? "已解决" : "未解决");
                jo.replace("isDeal", jsonObject1);
            });
            ifNotNullThen(x.getDate(), d -> jo.replace("date", ISO_DATETIME_FORMAT.format(d)));
            return jo;
        }));
    }

    /**
     * 12.2.2 删除
     *
     * @Author mu.jie
     * @Date 2016/11/23
     */
    @RequestMapping(value = "/del", method = POST)
    public Result delComplainList(String id) {
        complainService.delComplainList(id);
        return OK("success");
    }

    /**
     * 12.2.3 修改
     *
     * @Author mu.jie
     * @Date 2016/11/23
     */
    @RequestMapping(value = "/update", method = POST)
    public Result updateComplain(Long id, BooleanEnum complainStatus, String opinion) {
        complainService.updateComplain(id, complainStatus, opinion);
        return OK("success");
    }

    /**
     * 12.2.4 详情
     *
     * @Author mu.jie
     * @Date 2016/11/23
     */
    @RequestMapping(value = "/info", method = GET)
    public Result complainInfo(Long id) {
        String[] COMPLAIN_JSON = {"complainant.realName:username", "complainant.mobile:mobile", "complainant.hpAccount:hpAccount", "type.title:type", "defendant.realName:complainUser", "reason", "date:createDate"};
        String[] COMPLAINSTATUS_JSON = {"complainStatus", "opinion:reson"};
        JSONObject body = new JSONObject();
        Complain one = complainService.findOne(id);
        JSONObject complainJson = propsFilter(one, COMPLAIN_JSON);
        ifNotNullThen(one.getDate(), d -> complainJson.replace("createDate", ISO_DATETIME_FORMAT.format(d)));
        body = parseOneComplain(one, body);

        JSONObject complainStatuJson = propsFilter(one, COMPLAINSTATUS_JSON);
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("key", one.getIsDeal());
        jsonObject1.put("text", one.getIsDeal().getTitle());
        complainStatuJson.replace("complainStatus", jsonObject1);
        body.put("complain", complainJson);
        body.put("complainStatus", complainStatuJson);
        return OK(body);
    }

    private JSONObject parseOneComplain(Complain one, JSONObject body) {
        String[] USER_JSON = {"defendant.headImage.path:headImg", "defendant.id:vipId", "defendant.mobile:mobile", "defendant.realName:username", "defendant.username:nickName",
                "defendant.gender.title:gender", "defendant.birthday:birthday", "defendant.hpAccount:hpAccount", "referrer", "regDate", "isEntrepreneurs", "isPartner"};
        String[] SHOP_JSON = {"shop.name:name", "shop.id:id", "shop.owner.realName:contacts", "shop.owner.mobile:mobile", "shop.owner.hpAccount:hpAccount",
                "shop.ownerPosition.title:position", "area", "shop.address:address", "xyz", "shop.date:createDate"};
        String[] SCHOOL_JSON = {"school.id:id", "school.member.hpAccount:account", "school.member.realName:username", "school.member.gender.title:gender", "date:regDate",
                "school.member.mobile:mobile", "school.member.role.title:accountType", "school.name:name", "school.location:address", "xyz", "school.link:officialWebsite"};
        JSONObject userJson = null;
        JSONObject shopJson = null;
        JSONObject schoolJson = null;
        if (one.getType() == COMPLAINT_TO_USER || one.getType() == STORE_COMPLAINT_TO_USER_UNCONFIRM_RESIGN) {
            userJson = propsFilter(one, USER_JSON);

            Member defendant = one.getDefendant();
            final JSONObject finalUserJson = userJson;
            ifNotNullThen(defendant, d -> {
                MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(defendant.getToken());
                List<Partner> partner = partnerRepository.findByMemberTokenOrderByDate(defendant.getToken());
                ifNotNullThen(defendant.getBirthday(), x -> finalUserJson.replace("birthday", ISO_DATE_FORMAT.format(x)));
                ifNotNullThen(memberExtInfo.getRecommendUser(), x -> finalUserJson.replace("referrer", x.getRealName()));
                ifNotNullThen(memberExtInfo.getRegisterTime(), x -> finalUserJson.replace("regDate", ISO_DATE_FORMAT.format(x)));
                ifNotNullThen(defendant.getEntrepreneurLevel(), x -> finalUserJson.replace("isEntrepreneurs", x.getTitle()));
                finalUserJson.replace("isPartner", partner == null ? "否" : "是");
                ifNotNullThen(d.getUsername(), x -> finalUserJson.replace("nickName", decodePathVariable(x)));
            });
        } else if (one.getType() == COMPLAINT_TO_STORE || one.getType() == USER_COMPLAINT_TO_STORE_UNCONFIRM_WORK || one.getType() == ENTREPRENEUR_COMPLAINT_WORK || one.getType() == PARTNER_COMPLAINT_WORK) {
            shopJson = propsFilter(one, SHOP_JSON);
            Shop shop = one.getShop();
            final JSONObject finalShopJson = shopJson;
            ifNotNullThen(shop, s -> {
                ifNotNullThen(shop.getDate(), t -> finalShopJson.replace("createDate", ISO_DATE_FORMAT.format(t)));
                StringBuffer area = new StringBuffer();
                ifNotNullThen(shop.getProvince(), t -> area.append(shop.getProvince().getName()));
                ifNotNullThen(shop.getCity(), t -> area.append("-").append(shop.getCity().getName()));
                ifNotNullThen(shop.getArea(), t -> area.append("-").append(shop.getArea().getName()));
                StringBuffer xyz = new StringBuffer();
                ifNotNullThen(shop.getLongitude(), t -> xyz.append(t));
                ifNotNullThen(shop.getLatitude(), t -> xyz.append(",").append(t));
                finalShopJson.replace("area", area.toString());
                finalShopJson.replace("xyz", xyz.toString());
            });

        } else if (one.getType() == COMPLAINT_TO_SCHOOL) {
            schoolJson = propsFilter(one, SCHOOL_JSON);
            School school = one.getSchool();
            final JSONObject finalSchoolJson = schoolJson;
            ifNotNullThen(school, c -> {
                ifNotNullThen(school.getDate(), x -> finalSchoolJson.replace("regDate", ISO_DATE_FORMAT.format(x)));
                StringBuffer xyz = new StringBuffer();
                ifNotNullThen(c.getLongitude(), t -> xyz.append(t));
                ifNotNullThen(c.getLatitude(), t -> xyz.append(",").append(t));
                finalSchoolJson.replace("xyz", xyz.toString());
            });
        }
        body.put("user", userJson);
        body.put("shop", shopJson);
        body.put("school", schoolJson);
        return body;
    }
}
