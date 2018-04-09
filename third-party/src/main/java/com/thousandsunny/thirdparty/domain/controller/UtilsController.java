package com.thousandsunny.thirdparty.domain.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.core.ModuleKey.PhoneType;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.thirdparty.domain.service.UtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleTips.TIP_NO_CORRESPONDING_SPACE_WAS_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api/portal/utils", produces = APPLICATION_JSON_UTF8_VALUE)
public class UtilsController {

    private final static String[] REGION_JSON = {"id", "name"};
    private final static String[] VERSION_JSON = {"updateLog", "updateDate:date", "minVersion", "version:currentVersion"};
    @Autowired
    private UtilsService utilsService;

    /**
     * 生成图片token
     */
    @RequestMapping(value = "/token/{scope}", method = GET)
    public ResponseEntity circleToken(@PathVariable String scope, String key) {
        String token = utilsService.uploadToken(scope, key);
        ifTrueThrow(token == null, TIP_NO_CORRESPONDING_SPACE_WAS_FOUND);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", token);
        return ok(jsonObject);
    }

    /**
     * 检查版本
     */
    @RequestMapping(value = "/version", method = GET)
    public ResponseEntity checkVersion(PhoneType type) {
        return ok(propsFilter(utilsService.checkVersion(type), VERSION_JSON));
    }

    /**
     * 三级省市区
     */
    @RequestMapping(value = "/region/all", method = GET)
    public ResponseEntity cascadeRegion() {
        return ok(listToJson(utilsService.cascadeAllRegions()));
    }


    /**
     * 省市区列表
     */
    @RequestMapping(value = "/region", method = GET)
    public ResponseEntity region(Long provinceId, Long cityId) {
        List<Region> list = utilsService.region(provinceId, cityId);
        List<JSONObject> jsonObjects = simpleMap(list, x -> propsFilter(x, REGION_JSON));
        return ok(listToJson(jsonObjects));
    }

}
