package com.thousandsunny.common;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.model.Human;
import com.thousandsunny.core.model.Member;
import org.junit.Test;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.github.stuxuhai.jpinyin.PinyinHelper.getShortPinyin;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Created by guitarist on 7/5/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class ChineseUtil {

    public static String firstLetter(String word) {
        if (isNull(word))
            return "#";
        return getShortPinyin(word).substring(0, 1);
    }

    /**
     * 将Unicode编码转变成中文
     */
    public static JSONObject convert(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return parseObject(str);
    }


    public static <T> List<Map<String, Object>> groupBy(List<T> list, Function<T, String> supplier) {
        return list.stream().collect(groupingBy(o -> firstLetter(supplier.apply(o)))).entrySet().stream()
                .map(stringListEntry -> {
                    Map<String, Object> jsonObject = new HashMap<>();
                    jsonObject.put("letter", stringListEntry.getKey().toUpperCase());
                    jsonObject.put("list", stringListEntry.getValue());
                    return jsonObject;
                }).collect(toList());
    }

    public static <T> JSONObject groupBy(Page<T> page, Function<T, String> supplier) {
        List list = page.getContent().stream().collect(groupingBy(o -> firstLetter(supplier.apply(o)))).entrySet().stream()
                .map(stringListEntry -> {
                    Map<String, Object> jsonObject = new HashMap<>();
                    jsonObject.put("letter", stringListEntry.getKey().toUpperCase());
                    jsonObject.put("list", stringListEntry.getValue());
                    return jsonObject;
                }).collect(toList());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("last", page.isLast());
        jsonObject.put("list", list);
        return jsonObject;
    }

    @Test
    public void testGroupBy() {
        Member members1 = new Member();
        members1.setRealName("a");
        Member members2 = new Member();
        members2.setRealName("b");
        JsonUtil.prettyPrinter(groupBy(newArrayList(members1, members2), Human::getRealName));
    }

}
