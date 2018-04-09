package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.service.model.Advertisement;
import com.thousandsunny.service.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static com.thousandsunny.service.ModuleKey.AdTypeEnum.*;
import static com.thousandsunny.service.ModuleKey.AdStatusEnum.AD_OUTER;

/**
 * Created by Xiaoxuewei on 2016/11/30.
 */
@RestController
@RequestMapping(value = "/api/portal/advertisement", produces = APPLICATION_JSON_UTF8_VALUE)
public class AdvertisementController {

    private String[] index_advertisement_json = {
            "id",
            "type",
            "pic.path:imageUrl",
            "status",
    };

    private String[] start_advertisement_json = {
            "type",
            "pic.path:imageUrl",
    };

    @Autowired
    private AdvertisementService advertisementService;

    @RequestMapping(value = "/indexAdvertisementList", method = GET)
    public ResponseEntity indexAdvertisementList(Long provinceId, Long cityId, Long areaId) {
        List<Advertisement> list = advertisementService.findIndexAdvertisementList(provinceId, cityId, areaId);
        List<JSONObject> list_ = simpleMap(list, e -> {
            JSONObject jo = propsFilter(e, index_advertisement_json);
            if (e.getStatus() == AD_OUTER) jo.put("linkUrl", e.getLink());
            return advertisementTypeCheck(jo, e);
        });
        if (!CollectionUtils.isEmpty(list_))
            return ok(listToJson(list_));
        else return ok(listToJson(new ArrayList()));
    }


    @RequestMapping(value = "/startAdvertisement", method = GET)
    public ResponseEntity startAdvertisement() {
        Advertisement advertisement = advertisementService.findStartAdvertisement();
        JSONObject jsonObject = propsFilter(advertisement, start_advertisement_json);
        return ok(advertisementTypeCheck(jsonObject, advertisement));
    }

    /**
     * 根据广告类型返回对应的数据
     *
     * @param jo
     * @param e
     * @return
     */
    public JSONObject advertisementTypeCheck(JSONObject jo, Advertisement e) {
        if (isNotNull(e)) {
            if (e.getType() == AD_ARTICLE) {//资讯
                jo.put("tableId", e.getArticle().getId());
            } else if (e.getType() == AD_VIP) {//会员
                if (isNotNull(e.getMember())) {
                    jo.put("userToken", e.getMember().getToken());
                }
            } else if (e.getType() == AD_SHOP) {//店铺
                if (isNotNull(e.getShop())) {
                    jo.put("tableId", e.getShop().getId());
                }
            } else if (e.getType() == AD_JOB) {//工作
                if (isNotNull(e.getJob())) {
                    jo.put("tableId", e.getJob().getId());
                }
            } else if (e.getType() == AD_VIDEO) {//视频
                if (isNotNull(e.getVideo())) {
                    jo.put("tableId", e.getVideo().getId());
                }
            } else if (e.getType() == AD_SCHOOL) {//学校
                if (isNotNull(e.getSchool())) {
                    jo.put("tableId", e.getSchool().getId());
                }
            } else if (e.getType() == AD_COURSE) {//课程
                if (isNotNull(e.getCourse())) {
                    jo.put("tableId", e.getCourse().getId());
                }
            }
        }
        return jo;
    }
}
