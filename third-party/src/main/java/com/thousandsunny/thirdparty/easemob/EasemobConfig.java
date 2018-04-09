package com.thousandsunny.thirdparty.easemob;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by guitarist on 6/21/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Component
@ConfigurationProperties(prefix = "easemob")
public class EasemobConfig {
    private String protocal;

    private String host;

    private String org;

    private String app;

    private String clientId;

    private String clientSecret;

    private String impLib;

    private String appkey;

    private String defaultOwner;

    private String saltpass;
}
