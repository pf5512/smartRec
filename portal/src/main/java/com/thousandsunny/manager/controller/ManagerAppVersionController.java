package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.AppVersion;
import com.thousandsunny.service.model.Resume;
import com.thousandsunny.service.service.AppVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.PhoneType;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2017/1/16.
 */
@RestController
@RequestMapping(value = "/api/manager/version", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerAppVersionController {

    private final static String[] app_json = {"id", "phoneType", "version", "minVersion", "updateDate"};

    @Autowired
    private AppVersionService appVersionService;

    @RequestMapping(method = GET)
    public Result findAppVersionList(BackPageVo backPageVo, PhoneType phoneType) {
        Page<AppVersion> appVersionList = appVersionService.findAppVersionList(backPageVo, phoneType);
        Page<JSONObject> body = appVersionList.map(x -> {
            JSONObject jo = propsFilter(x, app_json);
            ifNotNullThen(x.getUpdateDate(), t -> jo.replace("updateDate", ISO_DATETIME_FORMAT.format(t)));
            return jo;
        });
        return OK(body);
    }

    @RequestMapping(method = DELETE)
    public Result delAppVersion(String id){
        return OK(appVersionService.delAppVersion(id));
    }

    @RequestMapping(method = POST)
    public Result addAppVersion(PhoneType type,String version,String lowestVersion){
        appVersionService.addAppVersion(type,version,lowestVersion);
        return OK();
    }
}
