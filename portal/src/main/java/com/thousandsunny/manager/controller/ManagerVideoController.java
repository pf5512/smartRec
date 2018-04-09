package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.service.model.Video;
import com.thousandsunny.service.model.VideoCommentary;
import com.thousandsunny.service.model.VideoFavorites;
import com.thousandsunny.service.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static com.thousandsunny.service.ModuleTips.TIP_NO_VIDEO;
import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;

/**
 * Created by admin on 2016/10/12.
 */
@RestController
@RequestMapping(value = "/api/manager/videos", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerVideoController {
    String[] video_list_json = {
            "id:id",
            "video.path:vedioID",
            "title:title",
            "createTime",
            "videoType",
            "sord:no",
            "state"

    };

    String[] member_json = {
            "id",
            "member.realName:name",
            "member.username:nickname",
            "member.mobile:phone",
            "member.hpAccount:hpAccount",
            "commentary.content:content",
            "date:createTime",
            "video.state:state"
    };
    String[] videoFavorites_json = {
            "id",
            "member.realName:username",
            "member.username:nickname",
            "member.mobile:phone",
            "member.hpAccount:hpAccount",
            "favoriteDate:createTime"
    };


    @Autowired
    private VideoService videoService;


    /**
     * 视频列表
     */
    @RequestMapping(value = "/video", method = GET)
    public Result list(BackPageVo pageVo, String text) {
        Page<Video> videoPage = videoService.listVideos(pageVo.pageRequest(), decodePathVariable(text));
        Page<JSONObject> jsonObject = videoPage.map(e -> {
            JSONObject jo = propsFilter(e, video_list_json);
            ifNotNullThen(e.getDate(), x -> jo.replace("createTime", ISO_DATETIME_FORMAT.format(x)));
            return jo;
        });
        return Result.OK(jsonObject);

    }

    /**
     * 删除
     */
    @RequestMapping(value = "/video", method = DELETE)
    public Result videoDelete(String id) {
        String[] strings = id.split(",");
        Arrays.asList(strings).forEach(e -> videoService.videoDelete(Long.parseLong(e)));
        return Result.OK();
    }


    /**
     * 新增/修改
     */

    @RequestMapping(value = "/video", method = POST)
    public Result addVideo(Video myVideo, Date createTime, String logoKey, String videoKey) {
        videoService.addVideo(myVideo, createTime, logoKey, videoKey);
        return Result.OK();
    }


    /**
     * 查看点赞列表
     */
    @RequestMapping(value = "/videoFavorites", method = GET)
    public Result listMemberFavorite(Long id, BackPageVo pageVo, String text) {
        Page<VideoFavorites> videoFavorites = videoService.getVideoFavorites(id, pageVo.pageRequest(), decodePathVariable(text));
        Page<JSONObject> jsonObject = videoFavorites.map(e -> {
            JSONObject jo = propsFilter(e, videoFavorites_json);
            ifNotNullThen(e.getFavoriteDate(), date -> jo.replace("createTime", ISO_DATETIME_FORMAT.format(date)));
            ifNotBlankThen(e.getMember().getUsername(), t -> jo.replace("nickname", decodePathVariable(t)));
            return jo;
        });
        return Result.OK(jsonObject);
    }

    /**
     * 查看评论列表
     */
    @RequestMapping(value = "/videoCommentary", method = GET)
    public Result commentVideo(Long id, BackPageVo pageVo, String text) {
        Page<VideoCommentary> videoCommentaryPage = videoService.comment(id, pageVo.pageRequest(), text);
        Page<JSONObject> jsonObject = videoCommentaryPage.map(e -> {
            JSONObject jo = propsFilter(e, member_json);
            ifNotNullThen(e.getDate(), date -> jo.replace("createTime", ISO_DATETIME_FORMAT.format(date)));
            ifNotBlankThen(e.getMember().getUsername(), t -> jo.replace("nickname", decodePathVariable(t)));
            return jo;
        });
        return Result.OK(jsonObject);
    }

    /**
     * 评论启用
     */
    @RequestMapping(value = "/enabled", method = POST)
    public Result enabledComment(Long videoId, Long id) {
        videoService.enabled(videoId, id);
        return OK();
    }

    /**
     * 5.1.7 详情
     *
     * @Author xiao xue wei
     * @Date 2017/3/16
     */
    @RequestMapping(value = "/detail", method = GET)
    public Result videoDetail(Long id) {
        String[] VIDEO_DETAIL_INFO = {"videoType", "title", "content", "sord", "video.path:videoKey", "logo.path:logoKey", "createTime"};
        Video video = videoService.findOne(id);
        ifFalseThrow(isNotNull(video) && video.getIsDelete() == ModuleKey.BooleanEnum.NO, TIP_NO_VIDEO);
        JSONObject jsonObject = propsFilter(video, VIDEO_DETAIL_INFO);
        ifNotNullThen(video.getDate(), e -> jsonObject.replace("createTime", ISO_DATETIME_FORMAT.format(e)));
        return OK(jsonObject);
    }

}
