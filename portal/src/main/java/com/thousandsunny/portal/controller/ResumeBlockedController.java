package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.ResumeBlocked;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.service.ResumeBlockedService;
import com.thousandsunny.service.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/portal/resumeBlocked", produces = APPLICATION_JSON_UTF8_VALUE)
public class ResumeBlockedController {


    private static final String[] BLOCKED_MODEL = {
            "id",
            "resumeMember.mobile:phoneNumber"
    };

    private static final String[] SHOP_JSON = {
            "id",
            "name:storeName",
            "owner.mobile:phoneNumber",
            "owner.realName:storeManagerUserRealName"
    };
    @Autowired
    private ResumeBlockedService resumeBlockedService;
    @Autowired
    private ShopService shopService;

    private static final String[] MEMBER_INFO = {
            "id",
            "mobile:phoneNumber"
    };

    /**
     * 屏蔽查看简历的用户列表
     */
    @RequestMapping(value = "/blockedList", method = GET)
    public ResponseEntity blockedList(String userToken) {
        List<ResumeBlocked> members = resumeBlockedService.blockedMember(userToken);
        List<JSONObject> jos = simpleMap(members, e -> {
            JSONObject jo = propsFilter(e, BLOCKED_MODEL);
            Shop shop = shopService.findByOwnerId(e.getResumeMember().getId());
            ifNotNullThen(shop, s -> jo.put("storeName", s.getName()));
            jo.put("storeManagerUserRealName",e.getResumeMember().getRealName());
            return jo;
        });
        return ok(listToJson(jos));
    }


    /**
     * 5.10新增屏蔽用户查看我的简历
     */
    @RequestMapping(value = "/blockedShop", method = POST)
    public ResponseEntity blockedShop(String userToken, String phoneNumber, String invitedUserToken) {
        Shop shop = resumeBlockedService.blocked(userToken, phoneNumber, invitedUserToken);
        JSONObject jo = propsFilter(shop,SHOP_JSON);
        return ok(jo);
    }


    /**
     * 取消屏蔽店铺
     */
    @RequestMapping(value = "/cancelBlockedShop", method = DELETE)
    public ResponseEntity cancelBlockedShop(String userToken, Long id, String invitedUserToken) {
        resumeBlockedService.cancelBlocked(userToken, id, invitedUserToken);
        return OK;
    }

}
