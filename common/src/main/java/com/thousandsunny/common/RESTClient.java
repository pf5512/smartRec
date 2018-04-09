package com.thousandsunny.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.parseMediaType;

/**
 * Created by guitarist on 4/20/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
public class RESTClient {

    private static final Logger _logger = LoggerFactory.getLogger(RESTClient.class);

    public static String get(Map uriVariables, String url) {
        ResponseEntity<String> entity = getResponseEntity(null, url, GET, null, uriVariables);
        return entity.getBody();
    }

    public static String get(Map uriVariables,
                             String url,
                             MultiValueMap<String, String> header) {
        ResponseEntity<String> entity = getResponseEntity(null, url, GET, header, uriVariables);
        return entity.getBody();
    }

    public static String post(MultiValueMap formData, String url) {
        ResponseEntity<String> entity = getResponseEntity(formData, url, POST, null, null);
        return entity.getBody();
    }

    public static String post(MultiValueMap formData, String url, Map uriVariables) {
        ResponseEntity<String> entity = getResponseEntity(formData, url, POST, null, uriVariables);
        return entity.getBody();
    }

    public static String post(MultiValueMap formData, String url, MultiValueMap<String, String> header) {
        ResponseEntity<String> entity = getResponseEntity(formData, url, POST, header, null);
        return entity.getBody();
    }

    public static String post(MultiValueMap formData, String url, MultiValueMap<String, String> header, Map uriVariables) {
        ResponseEntity<String> entity = getResponseEntity(formData, url, POST, header, uriVariables);
        return entity.getBody();
    }

    private static ResponseEntity<String> getResponseEntity(MultiValueMap<String, String> formData,
                                                            String url,
                                                            HttpMethod httpMethod,
                                                            MultiValueMap<String, String> headers,
                                                            Map<String, String> uriVariables) {
        HttpEntity httpEntity = new HttpEntity(formData, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> entity;
        if (isNull(uriVariables))
            entity = restTemplate.exchange(url, httpMethod, httpEntity, String.class);
        else
            entity = restTemplate.exchange(url, httpMethod, httpEntity, String.class, uriVariables);
        _logger.debug("请求的返回消息:{}:{}", entity.getStatusCode(), entity.getBody());
        return entity;
    }

    private static MultiValueMap<String, String> GBK2312() {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = parseMediaType("application/x-www-form-urlencoded; charset=GB2312");
        headers.setContentType(type);
        return headers;
    }

    private static MultiValueMap<String, String> UTF_8() {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = parseMediaType("application/x-www-form-urlencoded; charset=UTF-8");
        headers.setContentType(type);
        return headers;
    }
}
