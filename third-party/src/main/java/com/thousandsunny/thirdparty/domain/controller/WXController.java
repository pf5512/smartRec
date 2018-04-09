package com.thousandsunny.thirdparty.domain.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.domain.service.WxService;
import com.thousandsunny.thirdparty.wechat.entity.WXImageTextObj;
import com.thousandsunny.thirdparty.wechat.entity.WXObj;
import com.thousandsunny.thirdparty.wechat.entity.WXUserInfo;
import com.thousandsunny.thirdparty.wechat.util.WxConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.jta.UserTransactionAdapter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.alibaba.fastjson.JSON.toJSONString;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.HTMLUtil.encodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.thirdparty.wechat.util.WXUtil.*;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/wx", produces = APPLICATION_JSON_VALUE)
public class WXController {

    @Autowired
    private WxConfig wxConfig;
    @Autowired
    private WxService wxService;

    @RequestMapping(method = GET)
    public void service(HttpServletRequest request, HttpServletResponse response) {
        WXObj wxObj = extractWXObj(request, wxConfig.getFromUserName(), wxConfig.getToken());
        if (null != wxObj.getEchostr()) {
            replyEchostr(response, wxObj);//第一次验证
            return;
        }
        if (imageContainsQRCode(wxObj) || imageNotContainsCorrectQRCode(wxObj)) {
            List<WXImageTextObj> wxImageTextObjs = newArrayList(new WXImageTextObj("请填写信息", "请认真填写!", wxObj.getPicUrl(), ""));//标题,简介,点击的url
            replyImageText(response, wxObj, wxImageTextObjs);
        } else {
            replyText(response, wxObj, "");//返回的文字信息
        }
    }

    @RequestMapping(value = "/entry/gen", method = GET)
    public ModelAndView findOne(String code, ModelAndView view) {
        WXUserInfo userInfo = getAuth2UserInfo(getAuth2AccessTokenOpenId(wxConfig.getApp_id(), wxConfig.getSecret(), code));
        Member wxUserRef = wxService.checkExistIfNotCreate(userInfo);
        view.setViewName("redirect:" + wxConfig.getRedirectUrl() + "?token=" + wxUserRef.getToken());
        return view;
    }

    @RequestMapping(value = "/entry", method = GET)
    public ModelAndView findOneAnony(String code, ModelAndView view) {
        WXUserInfo userInfo = getAuth2UserInfo(getAuth2AccessTokenOpenId(wxConfig.getApp_id(), wxConfig.getSecret(), code));
//        Member wxUserRef= wxService.checkExistIfNotCreate(userInfo);
        JSONObject jo = propsFilter(userInfo, "openid", "nickname", "headimgurl");
        jo.replace("nickname", encodePathVariable(userInfo.getNickname()));
        view.setViewName("redirect:" + wxConfig.getRedirectUrl() + "?signNature=" + toJSONString(jo));
        return view;
    }

    /**
     * 微信的JS-SDK通过config接口注入权限验证配置
     *
     * @Author mu.jie
     * @Date 2017/2/14
     */
    @RequestMapping(value = "/config", method = GET)
    public ResponseEntity config(String url) {
        JSONObject body = wxService.config(url);
        return ok(body);
    }

}
