package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.service.model.Video;
import com.thousandsunny.service.model.VideoCommentary;
import com.thousandsunny.service.model.VideoFavorites;
import com.thousandsunny.service.repository.VideoRepository;
import com.thousandsunny.service.service.VideoService;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.thousandsunny.common.HTMLUtil.cleanHtmlTags;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.HTMLUtil.encodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by admin on 2016/10/12.
 */
@RestController
@RequestMapping(value = "/api/portal/video", produces = APPLICATION_JSON_UTF8_VALUE)
public class VideoController {
    String[] video_list_json = {
            "id:id",
            "logo.path:imgUrl",
            "video.path:videoUrl",
            "title:title",
            "date:date",
    };
    String[] video_search_json = {
            "id",
            "title"

    };
    String[] member_json = {
            "token:token",
            "headImage.path:headerImageUrl",
            "realName:realName",
            "username:nickName"
    };
    String[] video_detail_json = {
            "id:id",
            "logo.path:imgUrl",
            "video.path:videoUrl",
            "title:title",
            "content:content",
            "date:date"
    };
    String[] user_json = {
            "token",
            "headImage.path:headerImageUrl"
    };
    @Autowired
    private VideoService videoService;
    @Autowired
    private VideoRepository videoRepository;

    /**
     * 视频列表
     */
    @RequestMapping(value = "/list", method = GET)
    public ResponseEntity list(String userToken, PageVO pageVO) {
        Page<Video> videos = videoService.listVideos(pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(videos, e -> {
            JSONObject jb = propsFilter(e, video_list_json);
            Boolean isFavorites = videoService.isFavorite(userToken, e.getId());
            Boolean isCollects = videoService.isCollect(userToken, e.getId());
            jb.put("isLike", isFavorites);
            jb.put("isFavorited", isCollects);
            jb.put("likeCount", videoService.countLikeCount(e.getId()));
            return jb;
        });
        return ok(jsonObject);

    }

    /**
     * 视频搜索
     */
    @RequestMapping(value = "/search", method = GET)
    public ResponseEntity searchVideoList(String keyword, PageVO pageVO) {
        Page<Video> videos = videoService.searchedVideoList(decodePathVariable(keyword), pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(videos, x -> {
            JSONObject jb = propsFilter(x, video_search_json);
            return jb;
        });
        return ok(jsonObject);
    }

    /**
     * 视频点赞
     */
    @RequestMapping(value = "/favorite", method = POST)
    public ResponseEntity makeVideoFavorite(String userToken, Long id, OperatorType operatorType) {
        videoService.operateFavoriteVideo(userToken, id, operatorType);
        return OK;

    }

    /**
     * 视频收藏
     */
    @RequestMapping(value = "/collect", method = POST)
    public ResponseEntity collectVideo(String userToken, Long id, OperatorType operatorType) {
        videoService.operateCollectedVideo(userToken, id, operatorType);
        return OK;

    }

    /**
     * 视频点赞用户列表
     */
    @RequestMapping(value = "/memberFavorites", method = GET)
    public ResponseEntity listMemberFavorite(Long id, PageVO pageVO) {
        Page<VideoFavorites> videoFavorites = videoService.getMember_video_favorite(id, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(videoFavorites, e -> {
            JSONObject jb = propsFilter(e.getMember(), member_json);
            jb.put("date", e.getFavoriteDate());
            return jb;
        });
        return ok(jsonObject);

    }

    /**
     * 视频发表评论
     */
    @RequestMapping(value = "/comment", method = POST)
    public ResponseEntity commentVideo(String userToken, Long id, String content) {
        VideoCommentary videoCommentary = videoService.comment(userToken, id, encodePathVariable(content));
        JSONObject jsonObject = propsFilter(videoCommentary, "id");
        return ok(jsonObject);
    }

    /**
     * 视频评论列表
     */
    @RequestMapping(value = "/memberComments", method = GET)
    public ResponseEntity listMemberComment(Long id, PageVO pageVO) {
        Page<VideoCommentary> videoCommentaries = videoService.getMember_video_comment(id, pageVO.pageRequest());
        JSONObject jsonObject = pageToJson(videoCommentaries, e -> {
            JSONObject jb = propsFilter(e.getMember(), member_json);
            ifNotNullThen(e.getMember(), member -> ifNotBlankThen(member.getUsername(), name -> jb.replace("nickName", decodePathVariable(name))));
            jb.put("date", e.getDate());
            jb.put("id", e.getId());
            jb.put("content", decodePathVariable(e.getCommentary().getContent()));
            return jb;
        });
        return ok(jsonObject);
    }


    /**
     * 视频详情
     */
    @RequestMapping(value = "/detail", method = GET)
    public ResponseEntity showVideoDetails(String userToken, Long id) {
        Video video = videoRepository.findOne(id);
        JSONObject jsonObject = propsFilter(video, video_detail_json);
        ifNotBlankThen(video.getContent(), e -> jsonObject.replace("content", cleanHtmlTags(e)));
        Boolean isFavorites = videoService.isFavorite(userToken, video.getId());
        Boolean isCollects = videoService.isCollect(userToken, video.getId());
        jsonObject.put("isLike", isFavorites);
        jsonObject.put("isFavorited", isCollects);
        jsonObject.put("likeCount", videoService.getVideoFavorites(id).size());
        List<JSONObject> jsonObjects = simpleMap(videoService.Top10Favorites(id), e -> {
            JSONObject jo = propsFilter(e.getMember(), user_json);
            return jo;
        });
        jsonObject.put("likeUserTopList", jsonObjects);
        return ok(jsonObject);
    }

    /**
     * 3.9 视频删除评论
     *
     * @Author xiao xue wei
     * @Date 2017/3/1
     */
    @RequestMapping(value = "/videoDelete", method = DELETE)
    public ResponseEntity videoDelete(String userToken, Long id) {
        videoService.deleteVideo(userToken, id);
        return OK;
    }

}
