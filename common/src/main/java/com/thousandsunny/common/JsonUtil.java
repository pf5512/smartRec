package com.thousandsunny.common;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thousandsunny.common.entity.TitleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static org.apache.commons.beanutils.PropertyUtils.getProperty;

/**
 * Created by guitarist on 5/5/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private JsonUtil() {
    }

    public static void prettyPrinter(Object o) {
        try {
            logger.info(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void prettyPrinter(String marker, Object o) {
        try {
            logger.info(marker, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject propsFilter(Object obj, String... props) {
        JSONObject jsonObject = new JSONObject();
        newArrayList(props).forEach(prop -> {
            String[] namePair = prop.split(":");
            Object o = null;
            try {
                o = getProperty(obj, namePair[0]);
            } catch (Exception e) {
                ifNotNullThen(obj, i -> logger.error("类:{},属性:{}", obj.getClass(), namePair[0]));
                o = null;
            }
            jsonObject.put(namePair.length > 1 ? namePair[1] : namePair[0], o);
        });
        return jsonObject;
    }

    public static String toJsonString(Object o) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject enumToJson(TitleEnum e, JSONObject body, String key) {
        if (e == null) return null;
        JSONObject jo = new JSONObject();
        jo.put("key", e);
        jo.put("text", e.getTitle());
        body.put(key, jo);
        return body;
    }

    public static JSONObject valueIsNull(JSONObject body, Object value, String... key) {
        for (int i = 0; i < key.length; i++) {
            body.put(key[i], value);
        }
        return body;
    }

}