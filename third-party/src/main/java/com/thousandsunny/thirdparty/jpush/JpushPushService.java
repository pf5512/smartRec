package com.thousandsunny.thirdparty.jpush;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.ClientConfig;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.SMS;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import static cn.jpush.api.push.model.audience.Audience.alias;
import static cn.jpush.api.push.model.audience.Audience.tag;
import static com.alibaba.fastjson.JSON.toJSONString;
import static java.util.Objects.isNull;
import static org.hibernate.jpa.internal.EntityManagerImpl.LOG;

/**
 * Created by guitarist on 5/13/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */

@Setter
@Service
@ConfigurationProperties(prefix = "jpush")
public class JpushPushService {

    public static final String EXTRA = "extra";

    public boolean apnsProduction;
    public String secret;
    public String key;

    private JPushClient jPushClient;

    private JPushClient jpushClient() {
        if (isNull(jPushClient))
            jPushClient = new JPushClient(secret, key, null, clientConfig());
        return jPushClient;
    }

    public void pushByAlias(JPushEntity entity) {
        sendPush(jpushClient(), buildPushObject_android_and_ios(alias(entity.getFlags()), entity.getContent(), entity.getExtraEntity()));
    }

    public void pushByTags(JPushEntity entity) {
        sendPush(jpushClient(), buildPushObject_android_and_ios(tag(entity.getFlags()), entity.getContent(), entity.getExtraEntity()));
    }

    private ClientConfig clientConfig() {
        ClientConfig clientConfig = ClientConfig.getInstance();
        clientConfig.setApnsProduction(apnsProduction);
        return clientConfig;
    }


    private static void sendPush(JPushClient jPushClient, PushPayload pushPayload) {
        try {
            PushResult result = jPushClient.sendPush(pushPayload);
            LOG.info("Got result - " + result);
        } catch (APIConnectionException e) {
            LOG.error("Connection error. Should retry later. ", e);

        } catch (APIRequestException e) {
            LOG.error("Error response from JPush server. Should review and fix it. ", e);
            LOG.info("HTTP Status: " + e.getStatus());
            LOG.info("Error Code: " + e.getErrorCode());
            LOG.info("Error Message: " + e.getErrorMessage());
            LOG.info("Msg ID: " + e.getMsgId());
        }
    }

    private static PushPayload buildPushObject_android_and_ios(Audience audience, String content, Object extra) {
        String extraBody = toJSONString(extra);
        return PushPayload.newBuilder()
                .setSMS(SMS.content(content, 0))
                .setMessage(Message.content("message_content"))
                .setPlatform(Platform.android_ios())
                .setAudience(audience)
                .setNotification(Notification.newBuilder()
                        .setAlert(content)
                        .addPlatformNotification(AndroidNotification.newBuilder().setTitle("Android Title").addExtra(EXTRA, extraBody).build())
                        .addPlatformNotification(IosNotification.newBuilder().incrBadge(1).addExtra(EXTRA, extraBody).build())
                        .build()).build();
    }
}
