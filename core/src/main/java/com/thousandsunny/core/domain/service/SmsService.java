package com.thousandsunny.core.domain.service;

import com.thousandsunny.core.domain.repository.SmsRecordRepository;
import com.thousandsunny.core.model.SmsRecord;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static com.thousandsunny.common.RESTClient.post;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.SmsType;
import static com.thousandsunny.core.ModuleTips.TIP_SMS_CODE_WRONG;
import static com.thousandsunny.service.ModuleTips.TIP_NO_AUTHORITY;
import static com.thousandsunny.service.ModuleTips.TIP_NO_VERIFY_CODE;
import static com.thousandsunny.service.ModuleTips.TIP_VERIFY_CODE_FALSE;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * @描述
 * @作者 Guitarist
 * @创建时间：2015年12月30日 下午12:29
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Setter
@Service
@ConfigurationProperties(prefix = "sms.config")
public class SmsService extends BaseService<SmsRecord> {

    private static final String ACTION_SEND = "send";
    private final Logger _logger = LoggerFactory.getLogger("service");
    private Boolean open = true;
    private String agentUrl;
    private String senderId;
    private String senderAccount;
    private String senderPassword;
    private String signature;

    @Autowired
    private SmsRecordRepository smsRecordRepository;

    /**
     * 发送短信
     */
    @Async
    @Deprecated
    public void sendContent(String mobile, String content, Integer code, SmsType smsType) {
        SmsRecord smsRecord = new SmsRecord();
        smsRecord.setContent(content);
        smsRecord.setReceiver(mobile);
        smsRecord.setSmsType(smsType);
        smsRecord.setCode(code);
        if (open) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("action", ACTION_SEND);
            formData.add("userid", senderId);
            formData.add("account", senderAccount);
            formData.add("password", senderPassword);
            formData.add("mobile", mobile);
            formData.add("content", content + signature);
            String body = post(formData, agentUrl);
            _logger.info("Body:{}", body);
        }
        smsRecordRepository.save(smsRecord);

    }

    //判断验证码
    public void validateReceiverAndTypeAndCode(String mobile, SmsType smsType, Integer code) {
        SmsRecord smsRecord = smsRecordRepository.findByReceiverAndSmsTypeAndCodeOrderBySendDateDesc(mobile, smsType, code);
        ifNullThrow(smsRecord, TIP_NO_VERIFY_CODE);
        ifFalseThrow(smsRecord.getCode().equals(code), TIP_VERIFY_CODE_FALSE);
    }

    public void validateReceiverAndCode(String receiver, String code, SmsType smsType) {
        Page<SmsRecord> smsRecordResult = smsRecordRepository().findByReceiverAndSmsTypeOrderBySendDateDesc(receiver, smsType, new PageRequest(0, 1));
        ifTrueThrow(smsRecordResult.getContent().isEmpty(), TIP_SMS_CODE_WRONG);
        ifFalseThrow(valCodeIsEqual(code, smsRecordResult), TIP_SMS_CODE_WRONG);
    }

    private boolean valCodeIsEqual(String code, Page<SmsRecord> smsRecordResult) {
        return (smsRecordResult.getContent().get(0).getCode() + "").equals(code);
    }

    public List<SmsRecord> findSmsRecord() {
        ifFalseThrow(false,TIP_NO_AUTHORITY);
        return smsRecordRepository.findAll(new Sort(DESC, "sendDate"));
    }

    private SmsRecordRepository smsRecordRepository() {
        return (SmsRecordRepository) baseRepository;
    }
}