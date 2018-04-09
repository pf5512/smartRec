package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.model.Article;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.AdCategoryEnum;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.ifNotNullThen;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by mu.jie on 2016/11/23.
 */
@RestController
@RequestMapping(value = "/api/manager/ad", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerAdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    /**
     * 8.1.1 广告列表
     *
     * @Author mu.jie
     * @Date 2016/11/24
     */
    @RequestMapping(method = GET)
    public Result findAdList(BackPageVo backPageVo, String text, Long provinceId, Long cityId, Long areaId, AdCategoryEnum adType, Date startTime, Date endTime) {
        String[] JSON = {"id", "name", "category.title:category", "status.title:status", "showarea", "weight", "startTime", "endTime"};
        Page<Advertisement> page = advertisementService.findAdList(backPageVo, decodePathVariable(text), provinceId, cityId, areaId, adType, startTime, endTime);
        return OK(page.map(x -> {
            JSONObject jo = propsFilter(x, JSON);
            StringBuffer showArea = new StringBuffer();
            ifNotNullThen(x.getProvince(), t -> showArea.append(t.getName()));
            ifNotNullThen(x.getCity(), t -> showArea.append("-").append(t.getName()));
            ifNotNullThen(x.getArea(), t -> showArea.append("-").append(t.getName()));
            jo.replace("showarea", showArea.toString());
            ifNotNullThen(x.getStartTime(), t -> jo.replace("startTime", ISO_DATETIME_FORMAT.format(t)));
            ifNotNullThen(x.getEndTime(), t -> jo.replace("endTime", ISO_DATETIME_FORMAT.format(t)));
            jo.put("valid", x.getValid() == YES);
            return jo;
        }));
    }

    /**
     * 8.1.2 启用
     *
     * @Author mu.jie
     * @Date 2016/11/24
     */
    @RequestMapping(value = "/valid", method = POST)
    public Result validAdList(Long id) {
        Advertisement one = advertisementService.findOne(id);
        if (one.getValid() != YES) {
            one.setValid(YES);
        } else {
            one.setValid(NO);
        }
        advertisementService.save(one);
        return OK("success");
    }

    /**
     * 8.1.3 删除
     *
     * @Author mu.jie
     * @Date 2016/11/24
     */
    @RequestMapping(value = "/del", method = POST)
    public Result delAd(String id) {
        advertisementService.delAd(id);
        return OK("success");
    }

    /**
     * 8.1.4 新增/修改
     *
     * @Author mu.jie
     * @Date 2016/11/24
     */
    @RequestMapping(value = "/update", method = POST)
    public Result updateAdList(Advertisement ad, Long no, String img) {
        advertisementService.updateAdList(ad, no, img);
        return OK();
    }

    /**
     * 8.1.5 详情
     *
     * @Author mu.jie
     * @Date 2016/11/24
     */
    @RequestMapping(value = "/info", method = GET)
    public Result adInfo(Long id) {
        String[] JSON = {"category:class", "name", "status:adProperty", "type:adType", "IDNO", "showWay", "link:linkAdress",
                "province.id:province", "city.id:city", "area.id:area", "weight:no", "startTime", "endTime", "valid:state", "img"};
        Advertisement ad = advertisementService.findOne(id);
        JSONObject body = propsFilter(ad, JSON);
        body = parseIDNO(ad, body);
        final JSONObject finalBody = body;
        ifNotNullThen(ad.getStartTime(), t -> finalBody.replace("startTime", ISO_DATE_FORMAT.format(t)));
        ifNotNullThen(ad.getEndTime(), t -> finalBody.replace("endTime", ISO_DATE_FORMAT.format(t)));
        body.replace("state", ad.getValid() == YES);
        ifNotNullThen(ad.getPic(), t -> {
            JSONObject jo = new JSONObject();
            jo.put("key", t.getPath());
            finalBody.replace("img", jo);
        });
        return OK(body);
    }

    private JSONObject parseIDNO(Advertisement ad, JSONObject body) {
        if (ad.getType() == null) {
            return body;
        }
        switch (ad.getType()) {
            case AD_VIP:
                Member member = ad.getMember();
                ifNotNullThen(member, t -> body.replace("IDNO", t.getId()));
                break;
            case AD_SHOP:
                Shop shop = ad.getShop();
                ifNotNullThen(shop, t -> body.replace("IDNO", t.getId()));
                break;
            case AD_JOB:
                Job job = ad.getJob();
                ifNotNullThen(job, t -> body.replace("IDNO", t.getId()));
                break;
            case AD_COUPON:
                break;
            case AD_ARTICLE:
                Article article = ad.getArticle();
                ifNotNullThen(article, t -> body.replace("IDNO", t.getId()));
                break;
            case AD_VIDEO:
                Video video = ad.getVideo();
                ifNotNullThen(video, t -> body.replace("IDNO", t.getId()));
                break;
            case AD_SCHOOL:
                School school = ad.getSchool();
                ifNotNullThen(school, t -> body.replace("IDNO", t.getId()));
                break;
            case AD_COURSE:
                Course course = ad.getCourse();
                ifNotNullThen(course, t -> body.replace("IDNO", t.getId()));
                break;
        }
        return body;
    }
}
