package com.thousandsunny.thirdparty.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.wechat.entity.WXUserInfo;
import com.thousandsunny.thirdparty.wechat.util.WxConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.thousandsunny.common.RandomNumberUtil.randomUUIDString;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.service.ModuleTips.TIP_OPEN_ID_IS_NULL;
import static com.thousandsunny.thirdparty.wechat.util.WXUtil.getAccessToken;
import static com.thousandsunny.thirdparty.wechat.util.WXUtil.getTicket;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.codec.digest.DigestUtils.sha1;

@Service
public class WxService {
    @Autowired
    private BaseMemberService baseMemberService;
    @Autowired
    private WxConfig wxConfig;

    public static Cache<String, String> _7200SecondsContainer = newBuilder().maximumSize(1000)
            .expireAfterAccess(7200, SECONDS)
            .expireAfterWrite(7200, SECONDS).build();


    public Member checkExistIfNotCreate(WXUserInfo wxUserInfo) {
        ifNullThrow(wxUserInfo.getOpenid(), TIP_OPEN_ID_IS_NULL);
        Member member = baseMemberService.findByWxOpenId(wxUserInfo.getOpenid());
        if (isNull(member))
            member = baseMemberService.initialWithWxUser(wxUserInfo);
        return member;
    }

    public Member findByOpenId(String openId) {
        return baseMemberService.findByWxOpenId(openId);
    }

    public JSONObject config(String url) {
        String accessToken = null;
        String ticket = null;
        if (_7200SecondsContainer.size() > 0) {
            accessToken = _7200SecondsContainer.getIfPresent("accessToken");
            ticket = _7200SecondsContainer.getIfPresent("ticket");
        } else {
            accessToken = getAccessToken(wxConfig.getApp_id(), wxConfig.getSecret());
            ticket = getTicket(accessToken);
            _7200SecondsContainer.put("accessToken", accessToken);
            _7200SecondsContainer.put("ticket", ticket);
        }
        JSONObject body = new JSONObject();
        body.put("appId", wxConfig.getApp_id());
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        body.put("timestamp", timestamp);
        String noncestr = randomUUIDString().substring(0,16);
        body.put("nonceStr", noncestr);
        String str = "jsapi_ticket=" + ticket + "&noncestr=" + noncestr + "&timestamp=" + timestamp + "&url=" + url;
        body.put("signature", SHA1(str));
        return body;
    }

    public static String SHA1(String decript) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
