package com.thousandsunny.thirdparty.pingpp;

import com.pingplusplus.Pingpp;
import com.thousandsunny.core.ModuleKey;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by guitarist on 7/12/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Component
@ConfigurationProperties(prefix = "pingpp")
public class PingConfig {

    /**
     * Pingpp 管理平台对应的 API Key，api_key 获取方式：登录 [Dashboard](https://dashboard.pingxx.com)->点击管理平台右上角公司名称->开发信息-> Secret Key
     */
    private String apiKey;
    /**
     * Pingpp 管理平台对应的应用 ID，app_id 获取方式：登录 [Dashboard](https://dashboard.pingxx.com)->点击你创建的应用->应用首页->应用 ID(App ID)
     */
    private String appId;
    private String body;
    private String successUrl;

    @PostConstruct
    private void init() {
        // 设置 API Key
        Pingpp.apiKey = apiKey;
    }

}
