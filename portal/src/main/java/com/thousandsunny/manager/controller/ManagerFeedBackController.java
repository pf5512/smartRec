package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.service.FeedBackService;
import com.thousandsunny.core.domain.service.MemberExtInfoService;
import com.thousandsunny.core.model.FeedBack;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.model.Partner;
import com.thousandsunny.service.repository.PartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2016/11/23.
 */
@RestController
@RequestMapping(value = "/api/manager/feedBack", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerFeedBackController {

    @Autowired
    private FeedBackService feedBackService;
    @Autowired
    private MemberExtInfoService memberExtInfoService;
    @Autowired
    private PartnerRepository partnerRepository;
    private static final String[] FEED_BACK_JSON = new String[]{
            "id",
            "member.realName",
            "member.hpAccount",
            "member.mobile",
            "content",
            "date",
            "isDeal"
    };

    @RequestMapping(method = GET)
    public Result findFeedBack(BackPageVo backPageVo, String text, BooleanEnum tableType) {
        Page<FeedBack> page = feedBackService.findFeedBackList(backPageVo, decodePathVariable(text), tableType);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, FEED_BACK_JSON);
            ifNotNullThen(x.getIsDeal(), isDeal -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("key", x.getIsDeal());
                jsonObject.put("text", x.getIsDeal() == YES ? "已处理" : "待处理");

                jo.replace("isDeal", jsonObject);
            });
            ifNotNullThen(x.getDate(), t -> jo.replace("date", ISO_DATETIME_FORMAT.format(t)));
            return jo;
        }));
    }

    @RequestMapping(value = "/del", method = POST)
    public Result delFeedBack(String id) {
        feedBackService.delFeedBack(id);
        return OK("success");
    }

    @RequestMapping(value = "/update", method = POST)
    public Result updateFeedBack(Long id, BooleanEnum liuyanStatus, String reson) {
        feedBackService.updateFeedBack(id, liuyanStatus, reson);
        return OK("success");
    }

    @RequestMapping(value = "/info", method = GET)
    public Result feedBackInfo(Long id) {
        String[] LIUYAN_JSON = {"phoneNumber:mobile", "content", "date:createDate"};
        String[] PERSONALINFO_JSON = {"member.headImage.path:headImg", "member.id:vipId", "member.mobile:mobile", "member.realName:username",
                "member.username:nickName", "member.gender.title:gender", "member.birthday:birthday", "member.hpAccount:hpAccount",
                "referrer", "regDate", "isEntrepreneurs", "isPartner"};
        String[] LIUYANSTATUS_JSON = {"liuyanStatus", "opinion:reason"};
        FeedBack feedBack = feedBackService.findOne(id);
        JSONObject body = new JSONObject();
        JSONObject liuyanJson = propsFilter(feedBack, LIUYAN_JSON);
        liuyanJson.replace("createDate", ISO_DATETIME_FORMAT.format(feedBack.getDate()));

        JSONObject personalInfoJson = propsFilter(feedBack, PERSONALINFO_JSON);
        ifTrueThen(feedBack.getMember() != null && feedBack.getMember().getUsername() != null,
                () -> personalInfoJson.replace("nickName", decodePathVariable(feedBack.getMember().getUsername())));
        MemberExtInfo memberExtInfo = memberExtInfoService.findByMemberToken(feedBack.getMember().getToken());
        List<Partner> partner = partnerRepository.findByMemberTokenOrderByDate(feedBack.getMember().getToken());
        ifNotNullThen(memberExtInfo.getRecommendUser(), x -> personalInfoJson.replace("referrer", x.getRealName()));
        ifNotNullThen(memberExtInfo.getRegisterTime(), x -> personalInfoJson.replace("regDate", ISO_DATE_FORMAT.format(x)));
        ifNotNullThen(feedBack.getMember().getBirthday(), x -> personalInfoJson.replace("birthday", ISO_DATE_FORMAT.format(x)));
        personalInfoJson.replace("isEntrepreneurs", feedBack.getMember().getEntrepreneurLevel().getTitle());
        personalInfoJson.replace("isPartner", partner.isEmpty() ? "否" : "是");

        JSONObject liuyanStatusJson = propsFilter(feedBack, LIUYANSTATUS_JSON);
        ifNotNullThen(feedBack.getIsDeal(), x -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", x);
            jsonObject.put("text", x.getTitle());
            liuyanStatusJson.replace("liuyanStatus", jsonObject);
        });
        body.put("liuyan", liuyanJson);
        body.put("personalInfo", personalInfoJson);
        body.put("liuyanStatus", liuyanStatusJson);
        return OK(body);
    }
}
