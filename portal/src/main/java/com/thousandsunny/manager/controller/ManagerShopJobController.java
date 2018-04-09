package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;

import com.thousandsunny.service.model.Job;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.service.JobService;
import com.thousandsunny.service.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.JsonUtil.enumToJson;
import static com.thousandsunny.common.lambda.LambdaUtil.simpleMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static com.thousandsunny.service.ModuleKey.RecruitmentType;
import static com.thousandsunny.service.ModuleKey.RecruitmentState;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;

/**
 * Created by Xiaoxuewei on 2016/12/27.
 */
@RestController
@RequestMapping(value = "/api/manager/shopJob", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerShopJobController {

    private static final String[] JOB_LIST_INFO = {
            "shop.name",
            "id",
            "name",
            "recType",
            "reward",
            "state",
            "isEnableBoolean:isEnable",
    };

    @Autowired
    private JobService jobService;

    /**
     * 11.6.1 店铺岗位管理
     * @Author xiao xue wei
     * @Date 2016/12/27
     */
    @RequestMapping(value = "/shopJobList", method = GET)
    public Result shopJobList(BackPageVo pageVo, String text, RecruitmentType recruitmentType, RecruitmentState positionStatus) {
        Page<Job> pages = jobService.findJobList(pageVo, text, recruitmentType, positionStatus);
        return OK(pages.map(e -> {
            JSONObject jobJo = propsFilter(e, JOB_LIST_INFO);
            jobJo.put("date", ISO_DATETIME_FORMAT.format(e.getDate()));
            enumToJson(e.getRecType(), jobJo, "recType");
            enumToJson(e.getState(), jobJo, "state");
            return jobJo;
        }));
    }
}
