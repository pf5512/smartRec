package com.thousandsunny.thirdparty.qiniu;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static java.sql.Timestamp.valueOf;
import static java.time.LocalDateTime.now;
import static org.apache.commons.codec.digest.HmacUtils.hmacSha1;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.Base64Utils.encodeToUrlSafeString;

/**
 * Created by guitarist on 6/10/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Service
@Component
@ConfigurationProperties(prefix = "sc")
public class SevenCowUploadTokenService {
    private String accessKey;
    private String secretKey;

    /**
     * key:是七牛云图片的key,当key不为空时会覆盖七牛云上的图片
     */
    public String generateUploadToken(String scope, String key) {
        if (isNotBlank(key)) {
            scope = scope + ":" + key;
        }
        Long deadLine = valueOf(now().plusMinutes(10)).getTime() / 1000;
        String encoded = encodeToUrlSafeString(("{\"scope\":\"" + scope + "\",\"deadline\":" + deadLine + "}").getBytes());
        String encodedSigned = encodeToUrlSafeString(hmacSha1(secretKey.getBytes(), encoded.getBytes()));
        return accessKey + ":" + encodedSigned + ":" + encoded;
    }

}
