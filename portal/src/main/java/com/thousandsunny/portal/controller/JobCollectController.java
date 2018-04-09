package com.thousandsunny.portal.controller;


import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.service.CollectionsService;
import com.thousandsunny.service.service.SchoolPhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.service.ModuleKey.RecruitmentState.NORMAL;
import static org.apache.commons.lang3.StringUtils.join;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by Thinkpad on 2016/10/31.
 */
@RestController
@RequestMapping(value = "/api/portal/collections", produces = APPLICATION_JSON_UTF8_VALUE)
public class JobCollectController {

    String[] post_collection_json = {
            "id",
            "job.name:name",
            "job.id:jobId",
            "job.shop.name:storeName",
            "job.shop.logo.path:storeLogoImageUrl",
            "job.salary.name:salary",
            "job.period.name:workYear",
            "job.epmCount:unFindPeopleCount",
            "job.reward:rewardAmount",
            "job.recType:jobType",
            "job.shop.area.name:areaName",
            "collectEver:isInvalid"

    };
    String[] store_collection_json = {
            "id",
            "shop.id:storeId",
            "shop.name:name",
            "shop.logo.path:logo",
            "shop.address:address",
            "shop.province.name:provinceName",
            "shop.city.name:cityName",
            "shop.area.name:areaName"

    };
    String[] resume_collection_json = {
            "resume.member.realName:realName",
            "resume.member.headImage.path:hearderImageUrl",
            "resume.member.token:userToken",
            "position",
            "resume.intention.salary:salary",
            "resume.intention.workYear:workYear",
            "areaName"

    };

    String[] video_collection_json = {
            "id",
            "video.id:videoId",
            "video.logo.path:imageUrl",
            "video.video.path:videoUrl",
            "video.title:title",
            "video.date:date"

    };

    String[] picture_collection_json = {
            "picture.path:imageUrl",
            "owner.realName:userRealName",
            "owner.headImage.path:userHeaderImageUrl",
            "owner.token:userToken",
            "date"
    };


    @Autowired
    private CollectionsService collectionsService;
    @Autowired
    private SchoolPhotoService schoolPhotoService;


    /**
     * 岗位收藏
     */
    @RequestMapping(value = "/postcollect", method = GET)
    public ResponseEntity postCollect(String userToken, PageVO pageVO) {
        Page<JobCollect> postList = collectionsService.getPostCollectList(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(postList, e -> {
            JSONObject jo = propsFilter(e, post_collection_json);
            ifNotNullThen(e.getJob().getShop(), x -> {
                if (x.getArea() != null) {
                    jo.replace("areaName", x.getName());
                } else if (x.getCity() != null) {
                    jo.replace("areaName", x.getCity().getName());
                } else if (x.getProvince() != null) {
                    jo.replace("areaName", x.getArea().getName());
                } else {
                    jo.replace("areaName", "全国");
                }
            });

            if (e.getCollectEver() == YES || e.getJob().getState() != NORMAL || e.getJob().getIsDelete() == YES) {
                jo.put("isInvalid", true);
            } else {
                jo.put("isInvalid", false);
            }
            return jo;
        });
        return ok(jsonObject);
    }


    /**
     * 店鋪收藏
     */
    @RequestMapping(value = "/storecollect", method = GET)
    public ResponseEntity storeCollect(String userToken, PageVO pageVO) {
        Page<ShopCollect> shopList = collectionsService.getShopCollectList(userToken, pageVO.pageRequest());
        JSONObject jsonShop = pageToJson(shopList, x -> {
            JSONObject jo = propsFilter(x, store_collection_json);
            jo.put("isCooperate", false);
            jo.put("isTransfer", false);
            return jo;
        });
        return ok(jsonShop);
    }


    /**
     * 简历收藏
     */
    @RequestMapping(value = "/resumecollect", method = GET)
    public ResponseEntity resumeCollect(String userToken, PageVO pageVO) {

        Page<ResumeCollect> resumeList = collectionsService.getResumeCollectList(userToken, pageVO.pageRequest());

        JSONObject jsonObject = pageToJson(resumeList, e -> {
            JSONObject jo = propsFilter(e, resume_collection_json);
            ResumeIntention intention = e.getResume().getIntention();
            if (intention != null) {
                if (intention.getArea() != null) {
                    jo.replace("areaName", intention.getArea().getName());
                } else if (intention.getCity() != null) {
                    jo.replace("areaName", intention.getCity().getName());
                } else if (intention.getProvince() != null) {
                    jo.replace("areaName", intention.getArea().getName());
                }
            }
            final String[] position = {null};
            ifTrueThen(intention != null && intention.getJobTypes() != null, () -> intention.getJobTypes().forEach(t -> {
                String join = join(t.getName(), ",");
                position[0] = join.substring(0, join.length() - 1);
            }));
            jo.replace("position", position[0]);
            return jo;
        });
        return ok(jsonObject);
    }


    /**
     * 視頻收藏
     */
    @RequestMapping(value = "/videoCollect", method = GET)
    public ResponseEntity videoCollect(String userToken, PageVO pageVO) {

        Page<VideoCollect> videList = collectionsService.getVideoCollectList(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(videList, e -> propsFilter(e, video_collection_json));

        return ok(jsonObject);
    }


    /**
     * 图片收藏
     */
    @RequestMapping(value = "/pictureCollect", method = GET)
    public ResponseEntity pictureCollect(String userToken, PageVO pageVO) {
        Page<PictureCollect> pictureList = collectionsService.getPictureCollectList(userToken, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(pictureList, e -> propsFilter(e, picture_collection_json));
        return ok(jsonObject);
    }

    /**
     * 13.5我的学校收藏列表（二期）
     *
     * @Author xiao xue wei
     * @Date 2017/3/3
     */
    @RequestMapping(value = "/schoolCollect", method = GET)
    public ResponseEntity schoolCollect(String userToken, PageVO pageVO) {
        String[] school_collect_info = {"school.id:id", "school.name:name", "school.ispartSchoolBoolean:isCooperate", "address"};
        Page<SchoolCollect> schoolCollects = collectionsService.getSchoolCollect(pageVO.pageRequest(), userToken);
        JSONObject jsonObject = pageToJson(schoolCollects, schoolCollect -> {
            JSONObject jo = propsFilter(schoolCollect, school_collect_info);
            SchoolPhoto schoolPhoto = schoolPhotoService.findSchoolFirstPhoto(schoolCollect.getSchool());
            if (isNotNull(schoolPhoto)) jo.put("firstImageUrl", schoolPhoto.getPhoto().getPath());
            else jo.put("firstImageUrl", null);
            return jo;
        });
        return ok(jsonObject);
    }

    /**
     * 13.6我的课程收藏列表（二期）
     *
     * @Author xiao xue wei
     * @Date 2017/3/3
     */
    @RequestMapping(value = "/courseCollect", method = GET)
    public ResponseEntity courseCollect(String userToken, PageVO pageVo) {
        String[] course_collect_info = {"course.id:id", "course.school.id:schoolId", "course.school.name:schoolName", "course.name:name", "course.day:day",
                "course.price:price", "course.isPlatformCourseBoolean:isCooperate", "course.isEmploymentPlanningBoolean:isEmploymentPlanning"};
        Page<CourseCollect> page = collectionsService.findCourseCollect(userToken, pageVo.pageRequest());
        JSONObject jsonObject = pageToJson(page, courseCollect -> {
            JSONObject jo = propsFilter(courseCollect, course_collect_info);
            if (!courseCollect.getCourse().getPhotos().isEmpty()) {
                jo.put("firstImageUrl", courseCollect.getCourse().getPhotos().get(0).getPath());
            } else jo.put("firstImageUrl", null);
            return jo;
        });
        return ok(jsonObject);
    }
}
