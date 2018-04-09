package com.thousandsunny.thirdparty.wechat.util;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.thirdparty.wechat.entity.WXImageTextObj;
import com.thousandsunny.thirdparty.wechat.entity.WXObj;
import com.thousandsunny.thirdparty.wechat.entity.WXUserInfo;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.thousandsunny.common.RESTClient.get;

/**
 * Created by guitarist on 6/16/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class WXUtil {
    /**
     * 微信的返回信息
     */
    public static WXObj extractWXObj(HttpServletRequest request, String fromUserName, String token) {
        WXObj wxObj = new WXObj();
        wxObj.setSignature(request.getParameter("signature"));// 微信加密签名
        wxObj.setTimestamp(request.getParameter("timestamp")); // 时间戳
        wxObj.setNonce(request.getParameter("nonce"));// 随机数
        wxObj.setEchostr(request.getParameter("echostr"));// 随机字符串
        wxObj.setOpenid(request.getParameter("openid"));//用户id
        wxObj.setFromUserName(fromUserName);
        wxObj.setToken(token);
        try {
            Map<String, String> requestMap = parseXml(request);
            wxObj.setContent(requestMap.get("Content"));
            wxObj.setPicUrl(requestMap.get("PicUrl"));//
            wxObj.setMediaId(requestMap.get("MediaId"));
//            wxObj.setDecodedData(processDecode(wxObj.getPicUrl()));
            wxObj.setCreateTime(requestMap.get("CreateTime"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wxObj;
    }

    /**
     * 返回第一次的响应密文
     */
    public static void replyEchostr(HttpServletResponse response, WXObj wxObj) {
        try (PrintWriter out = response.getWriter()) {
            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
            if (SignUtil.checkSignature(wxObj)) {
                out.print(wxObj.getEchostr());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回文本
     */
    public static void replyText(HttpServletResponse response, WXObj wxObj, String content) {
        String finalContent = "<xml>" +
                "<ToUserName><![CDATA[" + wxObj.getOpenid() + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + wxObj.getFromUserName() + "]]></FromUserName>" +
                "<CreateTime>" + wxObj.getCreateTime() + "</CreateTime>" +
                "<MsgType><![CDATA[text]]></MsgType>" +
                "<Content><![CDATA[" + content + "]]></Content>" +
                "</xml>";
        try (PrintWriter out = response.getWriter()) {
            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
            if (SignUtil.checkSignature(wxObj)) {
                out.print(UTF_8_TO_ISO_8859_1(finalContent));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图片
     */
    public static void replyImage(HttpServletResponse response, WXObj wxObj, String mediaId) {
        //图片
        String finalContent = "<xml>" +
                "<ToUserName><![CDATA[" + wxObj.getOpenid() + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + wxObj.getFromUserName() + "]]></FromUserName>" +
                "<CreateTime>" + wxObj.getCreateTime() + "</CreateTime>" +
                "<MsgType><![CDATA[image]]></MsgType>" +
                "<Image>" +
                "<MediaId><![CDATA[" + mediaId + "]]></MediaId>" +
                "</Image>" +
                "</xml>";
        try (PrintWriter out = response.getWriter()) {
            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
            if (SignUtil.checkSignature(wxObj)) {
                out.print(UTF_8_TO_ISO_8859_1(finalContent));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回图文
     */
    public static void replyImageText(HttpServletResponse response, WXObj wxObj, List<WXImageTextObj> wxImageTextObjs) {
        //图片
        StringBuilder finalContent = new StringBuilder("<xml>" +
                "<ToUserName><![CDATA[" + wxObj.getOpenid() + "]]></ToUserName>" +
                "<FromUserName><![CDATA[" + wxObj.getFromUserName() + "]]></FromUserName>" +
                "<CreateTime>" + wxObj.getCreateTime() + "</CreateTime>" +
                "<MsgType><![CDATA[news]]></MsgType>" +
                "<ArticleCount>" + wxImageTextObjs.size() + "</ArticleCount>" +
                "<Articles>");

        wxImageTextObjs.forEach(e -> finalContent.append("<item>" +
                "<Title><![CDATA[" + e.getTitle() + "]]></Title>" +
                "<Description><![CDATA[" + e.getDescription() + "]]></Description>" +
                "<PicUrl><![CDATA[" + e.getPicUrl() + "]]></PicUrl>" +
                "<Url><![CDATA[" + e.getUrl() + "]]></Url>" +
                "</item>"));

        finalContent.append("</Articles></xml>");
        try (PrintWriter out = response.getWriter()) {
            // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
            if (SignUtil.checkSignature(wxObj)) {
                out.print(UTF_8_TO_ISO_8859_1(finalContent.toString()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getAccessToken(String appId, String secret) {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
        return parseObject(get(null, url)).getString("access_token");
    }

    public static String getTicket(String accessToken){
        String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+ accessToken +"&type=jsapi";//这个url链接和参数不能变
        return parseObject(get(null,url)).getString("ticket");
    }

    /**
     * auth2得到access_token,openId
     */
    public static JSONObject getAuth2AccessTokenOpenId(String appId, String secret, String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
        return parseObject(get(null, url));
    }

    /**
     * 得到微信用户信息
     */
    public static WXUserInfo getUserInfo(String accessToken, String openId) {
        String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";
        return parseObject(ISO_8859_1_TO_UTF_8(get(null, url)), WXUserInfo.class);
    }

    /**
     * auth2得到微信用户信息
     */
    public static WXUserInfo getAuth2UserInfo(String accessToken, String openId) {
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";
        return parseObject(ISO_8859_1_TO_UTF_8(get(null, url)), WXUserInfo.class);
    }

    /**
     * auth2得到微信用户信息
     */
    public static WXUserInfo getAuth2UserInfo(JSONObject jsonObject) {
        String accessToken = jsonObject.getString("access_token");
        String openId = jsonObject.getString("openId");
        return getAuth2UserInfo(accessToken, openId);
    }

    private static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        Map<String, String> map = new HashMap<>();
        try (InputStream inputStream = request.getInputStream()) {
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);
            Element root = document.getRootElement();
            List<Element> elementList = root.elements();
            for (Element e : elementList)
                map.put(e.getName(), e.getText());
        }
        return map;
    }

    private static String ISO_8859_1_TO_UTF_8(String content) {
        try {
            return new String(content.getBytes("iso-8859-1"), Charset.forName("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String UTF_8_TO_ISO_8859_1(String content) {
        try {
            return new String(content.getBytes("UTF-8"), Charset.forName("iso-8859-1"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean imageNotContainsCorrectQRCode(WXObj wxObj) {
        return null != wxObj.getMediaId() && null == wxObj.getDecodedData();
    }

    public static boolean imageContainsQRCode(WXObj wxObj) {
        return null != wxObj.getMediaId() && null != wxObj.getDecodedData();
    }

}


