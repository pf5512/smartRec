package com.thousandsunny.thirdparty.pingpp;

import com.pingplusplus.model.Event;
import com.thousandsunny.common.exception.BaseException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

import static com.pingplusplus.model.Webhooks.eventParse;
import static java.security.Signature.getInstance;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;
import static org.springframework.util.ResourceUtils.getFile;

/**
 * Created by guitarist on 7/12/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class PingppUtil {

    protected static Logger _logger = LoggerFactory.getLogger(PingppUtil.class);
    public static final String CHARGE_SUCCEEDED = "charge.succeeded";
    public static final String REFUND_SUCCEEDED = "refund.succeeded";

    public static Pair<String, Event> verifyRequest(HttpServletRequest request) {
        _logger.debug("验证开始");
        try {
            request.setCharacterEncoding("UTF8");
            // 获得 http body 内容
            BufferedReader reader = request.getReader();
            StringBuffer buffer = new StringBuffer();
            String string;
            while ((string = reader.readLine()) != null) {
                buffer.append(string);
            }
            reader.close();
            String body = buffer.toString();

            String signature = request.getHeader("X-Pingplusplus-Signature");
            boolean result = verifyData(body, signature, getPubKey());
            if (!result) {
                return of("fail", null);
            }
            // 解析异步通知数据
            Event event = eventParse(body);
            if (CHARGE_SUCCEEDED.equals(event.getType())) {
                return of(CHARGE_SUCCEEDED, event);
            } else if (REFUND_SUCCEEDED.equals(event.getType())) {
                return of(REFUND_SUCCEEDED, event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        _logger.debug("验证通过");
        return of("fail", null);
    }

    /**
     * 读取文件, 部署 web 程序的时候, 签名和验签内容需要从 request 中获得
     */
    public static String getStringFromFile(String filePath) throws Exception {
        FileInputStream in = new FileInputStream(filePath);
        InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
        BufferedReader bf = new BufferedReader(inReader);
        StringBuilder sb = new StringBuilder();
        String line;
        do {
            line = bf.readLine();
            if (line != null) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
        } while (line != null);

        return sb.toString();
    }

    /**
     * 获得公钥
     */
    public static PublicKey getPubKey() {
        try {
            String pubKeyString = getStringFromFile(getFile(CLASSPATH_URL_PREFIX + "pingpp_public_key.pem").getPath());
            pubKeyString = pubKeyString.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
            byte[] keyBytes = decodeBase64(pubKeyString);
            // generate public key
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);
            return publicKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new BaseException("获取公钥异常!");
    }

    /**
     * 验证签名
     */
    public static boolean verifyData(String dataString, String signatureString, PublicKey publicKey) {
        try {
            byte[] signatureBytes = decodeBase64(signatureString);
            Signature signature = getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(dataString.getBytes("UTF-8"));
            return signature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        throw new BaseException("验证签名失败!");
    }

}
