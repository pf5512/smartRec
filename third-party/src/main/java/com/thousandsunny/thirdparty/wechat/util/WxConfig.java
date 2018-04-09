package com.thousandsunny.thirdparty.wechat.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 如果这些代码有用，那它们是guitarist在24/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Component
@ConfigurationProperties(prefix = "wx_config")
public class WxConfig {

    private String app_id;

    private String secret;

    private String redirectUrl;

    private String fromUserName;
    
    private String token;
}
