package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.service.model.MemberChatProfile;
import com.thousandsunny.service.service.MemberChatProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Created by admin on 2016/10/12.
 */

@RestController
@RequestMapping(value = "/api/portal/memberChatProfile", produces = APPLICATION_JSON_UTF8_VALUE)
public class MemberChatProfileController {

    private final static String[] chatUser_detail_json = {
            "isTopBool:isTop",
            "isNoDisturbBool:isNoDisturb"
    };


    private static final String[] MEMBER_INFO = {
            "token",
            "realName",
            "headImage.path:headerImageUrl"
    };
    @Autowired
    private MemberChatProfileService memberChatProfileService;


    //获取单聊信息  已检查完
    @RequestMapping(value = "/findOneChatInfo", method = GET)
    public ResponseEntity findOneChatInfo(String userToken,
                                          String chatUserToken) {
        MemberChatProfile memberChatProfile = memberChatProfileService.findOneChatInfo(userToken, chatUserToken);
        JSONObject jsonObject = propsFilter(memberChatProfile.getChatUser(), MEMBER_INFO);
        JSONObject chatUserJsonObjects = propsFilter(memberChatProfile, chatUser_detail_json);
        chatUserJsonObjects.put("chatUserInfo", jsonObject);
        return ok(chatUserJsonObjects);

    }

    //单聊置顶  已检查完

    @RequestMapping(value = "/setTop", method = PUT)
    public ResponseEntity setIsTop(String userToken,
                                   String chatUserToken,
                                   OperatorType operatorType) {
        memberChatProfileService.setTop(userToken, chatUserToken, operatorType);
        return OK;

    }

    //单聊免打扰  已检查完

    @RequestMapping(value = "/noDisturb", method = PUT)
    public ResponseEntity noDisturb(String userToken,
                                    String chatUserToken,
                                    OperatorType operatorType) {
        memberChatProfileService.noDisturb(userToken, chatUserToken, operatorType);
        return OK;

    }


}
