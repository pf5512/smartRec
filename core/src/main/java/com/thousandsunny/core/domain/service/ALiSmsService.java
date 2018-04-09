package com.thousandsunny.core.domain.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.thousandsunny.core.domain.repository.SmsRecordRepository;
import com.thousandsunny.core.model.SmsRecord;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.thousandsunny.common.RandomNumberUtil.genValCode;
import static com.thousandsunny.core.ModuleKey.SmsType;
import static java.util.Objects.isNull;

/**
 * 如果这些代码有用，那它们是guitarist在07/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Setter
@Service
@ConfigurationProperties(prefix = "a_li_sms_sender")
public class ALiSmsService {
    private String accessKeyID;
    private String accessKeySecret;
    private String signName;
    private Boolean open = true;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SmsRecordRepository smsRecordRepository;

    public void sendContent(String mobile, SmsType smsType) {
        sendContent(mobile, smsType.getTemplateCode(), null, smsType);
    }

    public void sendContent(String mobile, SmsType smsType, Map<String, String> params) {
        sendContent(mobile, smsType.getTemplateCode(), params, smsType);
    }

    /**
     * 短信内容需要在模板中定义,通过不同的templateCode调用,短信的参数通过paramString传递
     */
    public void sendContent(String mobile, String templateCode, SmsType smsType) {
        sendContent(mobile, templateCode, null, smsType);
    }

    public void sendContent(String mobile, String templateCode, Map<String, String> params, SmsType smsType) {
        if (!open)
            return;

        if (isNull(params))
            params = new HashMap<>();

        Integer validateCode = null;
        if (smsType.getNeedValCode()) {//需要验证码.自动产生验证码
            validateCode = genValCode();
            params.put("code", validateCode + "");
        }
        try {
//            IClientProfile profile = getProfile("cn-hangzhou", accessKeyID, accessKeySecret);
//            addEndpoint("cn-hangzhou", "cn-hangzhou", "Sms", "sms.aliyuncs.com");
//            IAcsClient client = new DefaultAcsClient(profile);
//            SingleSendSmsRequest request = new SingleSendSmsRequest();
//            request.setSignName(signName);
//            request.setTemplateCode(templateCode);
//            request.setParamString(toJSONString(params));
//            request.setRecNum(mobile);
//            SingleSendSmsResponse httpResponse = client.getAcsResponse(request);
//
//            String jsonString = toJSONString(httpResponse);
            //初始化ascClient,暂时不支持多region
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyID, accessKeySecret);
            DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Dysmsapi", "dysmsapi.aliyuncs.com");
            IAcsClient acsClient = new DefaultAcsClient(profile);
            //组装请求对象
            SendSmsRequest request = new SendSmsRequest();
            //使用post提交
            request.setMethod(MethodType.POST);
            //必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
            request.setPhoneNumbers(mobile);
            //必填:短信签名-可在短信控制台中找到
            request.setSignName(signName);
            //必填:短信模板-可在短信控制台中找到
            request.setTemplateCode(templateCode);
            //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
            //友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
            request.setTemplateParam("{\"name\":\"" + mobile + "\", \"code\":\"" + validateCode + "\"}");
            //请求失败这里会抛ClientException异常
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            logger.debug("SingleSendSmsResponse:{}", sendSmsResponse.getMessage());

            saveSmsRecord(mobile, templateCode, validateCode, smsType);
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    private void saveSmsRecord(String mobile, String content, Integer code, SmsType smsType) {
        SmsRecord smsRecord = new SmsRecord();
        smsRecord.setContent(content);
        smsRecord.setReceiver(mobile);
        smsRecord.setSmsType(smsType);
        smsRecord.setCode(code);
        smsRecordRepository.save(smsRecord);
    }

}
