package com.thousandsunny.common;

import java.io.UnsupportedEncodingException;

import static com.github.stuxuhai.jpinyin.ChineseHelper.isChinese;
import static java.net.URLDecoder.decode;
import static java.net.URLEncoder.encode;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.jsoup.Jsoup.parse;

/**
 * Created by guitarist on 5/11/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class HTMLUtil {

    /**
     * 解码
     */
    public static String decodePathVariable(String keyWord) {
        if (keyWord == null) return null;
        if (keyWord.equals("%"))
            return keyWord;
        try {
            return decode(keyWord, "UTF-8");
        } catch (Exception e) {
            return keyWord;
        }
    }

    /**
     * 编码
     */
    public static String encodePathVariable(String keyWord) {
        if (keyWord == null) return null;
        try {
            return encode(keyWord, "UTF-8");
        } catch (Exception e) {
            return keyWord;
        }
    }

    /**
     * 英文和数字
     */
    public static Boolean pureEnglishNum(String pure) {
        return pure.matches("[a-zA-Z0-9]+");
    }

    public static String cleanHtmlTags(String rawContent) {
        return parse(rawContent).text();
    }

    public static Boolean pureChinese(String content) {
        if (isBlank(content))
            return false;
        for (int i = 0; i < content.length(); i++) {
            if (!isChinese(content.charAt(i)))
                return false;
        }
        return true;
    }
}
