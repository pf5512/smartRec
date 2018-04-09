package com.thousandsunny.common;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

import static com.google.code.kaptcha.Constants.KAPTCHA_SESSION_KEY;
import static com.thousandsunny.common.CacheableUtil._5MinutesContainer;
import static com.thousandsunny.common.CacheableUtil.cleanUp;
import static java.util.Objects.isNull;
import static javax.imageio.ImageIO.write;

/**
 * Created by guitarist on 6/15/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class KaptchaUtil {

    private static Producer captchaProducer = kaptcha();

    public static void kaptcha(HttpServletRequest request, HttpServletResponse response) {

        try (ServletOutputStream out = response.getOutputStream()) {
            HttpSession session = request.getSession();
            String code = (String) session.getAttribute(KAPTCHA_SESSION_KEY);
            System.out.println("验证码: " + code);

            wrapResponse(response);
            String capText = captchaProducer.createText();
            session.setAttribute(KAPTCHA_SESSION_KEY, capText);
            BufferedImage bi = captchaProducer.createImage(capText);
            ImageIO.write(bi, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cacheableKaptcha(String sessionKey, HttpServletResponse response) {
        cleanUp();
        if (isNull(sessionKey))
            return;
        wrapResponse(response);
        String capText = captchaProducer.createText();
        _5MinutesContainer.put(sessionKey, capText);
        BufferedImage bi = captchaProducer.createImage(capText);
        try (ServletOutputStream out = response.getOutputStream()) {
            write(bi, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void wrapResponse(HttpServletResponse response) {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
    }

    private static Producer kaptcha() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        properties.setProperty("kaptcha.image.width", "125");
        properties.setProperty("kaptcha.image.height", "45");
        properties.setProperty("kaptcha.textproducer.font.size", "45");
        properties.setProperty("kaptcha.session.key", "code");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        Config cf = new Config(properties);
        DefaultKaptcha producer = new DefaultKaptcha();
        producer.setConfig(cf);
        return producer;
    }

}
