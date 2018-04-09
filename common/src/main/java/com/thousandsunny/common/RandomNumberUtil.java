package com.thousandsunny.common;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Integer.parseInt;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.RandomStringUtils.randomAscii;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

/**
 * 随机数工具类
 */
public class RandomNumberUtil {

    /**
     * 生成短信验证码随机数
     */
    public static Integer genValCode() {
        Integer numeric = parseInt(randomNumeric(6));
        return (numeric + "").length() < 6 ? genValCode() : numeric;
    }

    public static String genSerialNo() {
        return "HP" + currentTimeMillis() + genValCode();
    }

    public static String genInitPwd() {
        return "TD" + genValCode();
    }

    public static String genSalt() {
        return randomAscii(6);
    }

    public static String randomUUIDString() {
        return randomUUID().toString().replaceAll("-", "");
    }

    @Test
    public void testGenValCode() {
        range(1, 1000).forEach(value -> System.out.println(genValCode()));
    }

    /**
     * 生成16位数字订单号
     * @Author mu.jie
     * @Date 2017/2/15
     */
    public static Integer count = 0;
    public static String getOrderNo() {
        if (count >=999) count=0;
        count++;
        return currentTimeMillis()+String.format("%03d",count);
    }

}

