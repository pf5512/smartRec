package com.thousandsunny.thirdparty.breach;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 违约金比例
 * Created by 13336 on 2017/1/10.
 */
@Data
@Component
@ConfigurationProperties(prefix = "breach")
public class BreachConfig {
    //违约金比例
    private double scale;
    //违约金基数
    private double minBreach;
}
