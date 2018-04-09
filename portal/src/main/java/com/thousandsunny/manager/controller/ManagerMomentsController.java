package com.thousandsunny.manager.controller;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.entity.Result;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.Moments;
import com.thousandsunny.service.model.MomentsFavorites;
import com.thousandsunny.service.repository.MomentsRepository;
import com.thousandsunny.service.service.MomentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.List;

import static com.thousandsunny.common.DateUtil.ISO_DATETIME_FORMAT;
import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.entity.Result.OK;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static java.util.Objects.isNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * Created by admin on 2016/11/22.
 */
@RestController
@RequestMapping(value = "/api/manager/moments", produces = APPLICATION_JSON_UTF8_VALUE)
public class ManagerMomentsController {
    String[] moments_list_json = {
            "id",
            "author.realName:userName",
            "author.username:nickName",
            "author.mobile",
            "author.hpAccount"
    };

    String[] moments_detail_json = {
            "author.realName:userName",
            "author.username:nickName",
            "author.mobile:tel",
            "author.hpAccount:hpAccount",
            "position:address",
    };
    String[] member_list_json = {
            "id",
            "realName:name",
    };
    String[] moments_like_json = {
            "id",
            "member.realName:userName",
            "member.username:nickName",
            "member.mobile",
            "member.hpAccount",
    };
    String[] commentary_json = {
            "id",
            "commentator.realName:userName",
            "commentator.username:nickName",
            "commentator.mobile",
            "commentator.hpAccount",
            "content",
            "state",
    };
    @Autowired
    private MomentsService momentsService;
    @Autowired
    private MomentsRepository momentsRepository;

    /**
     * 慧美圈管理列表
     */
    @RequestMapping(value = "/list", method = GET)
    public Result list(BackPageVo backPageVo, String text, Long provinceId, Long cityId, Long areaId) {
        Page<Moments> momentses = momentsService.momentses(backPageVo, decodePathVariable(text), provinceId, cityId, areaId);
        Page<JSONObject> jsonObject = momentses.map(e -> {
            JSONObject jo = propsFilter(e, moments_list_json);
            ifNotBlankThen(e.getAuthor().getUsername(), name -> jo.replace("nickName", decodePathVariable(name)));
            jo.put("content", new String(e.getContent(), Charset.forName("UTF-8")));
            List<CloudFile> pics = e.getPics();
            String imgurl = "";
            if (pics != null && pics.size() > 0) {
                imgurl = pics.get(0).getPath();
            }
            JSONObject ims = new JSONObject();
            ims.put("imgurl", imgurl);
            ims.put("contentType", e.getContentType().name());
            jo.put("imgurl", ims);
            jo.put("like", momentsService.countLikers(e.getId()));
            jo.put("discuss", momentsService.countComments(e.getId()));
            jo.put("createTime", ISO_DATETIME_FORMAT.format(e.getPublishTime()));
            if (e.getIsDelete() == ModuleKey.StateEnum.NO) {
                jo.put("state", true);
            }
            if (e.getIsDelete() == ModuleKey.StateEnum.HIDE) {
                jo.put("state", false);
            }
            return jo;
        });
        return OK(jsonObject);
    }

    /**
     * 删除
     */
    @RequestMapping(value = "/delete", method = POST)
    public Result delete(String ids) {
        momentsService.delete(ids);
        return OK();
    }

    /**
     * 启用
     */
    @RequestMapping(value = "/state", method = PUT)
    public Result state(Long id) {
        momentsService.state(id);
        return OK();
    }

    /**
     * 查看详情
     */
    @RequestMapping(value = "/detail", method = GET)
    public Result detail(Long id) {
        Moments moments = momentsRepository.findOne(id);
        JSONObject jsonObject = propsFilter(moments, moments_detail_json);
        ifNotBlankThen(moments.getAuthor().getUsername(), name -> jsonObject.replace("nickName", decodePathVariable(name)));
        jsonObject.put("content", new String(moments.getContent(), Charset.forName("UTF-8")));
        List<JSONObject> j1 = simpleMap(moments.getPics(), e -> propsFilter(e, "path:imgurl"));
        jsonObject.put("imgs", j1);
        jsonObject.put("contentType", moments.getContentType().name());
//        List<CloudFile> pics = moments.getPics();
//        String imgurl = "";
//        if(pics!=null&&pics.size()>0){
//            imgurl = pics.get(0).getPath();
//        }
//        jsonObject.put("imgurl",imgurl);
        List<JSONObject> j2 = simpleMap(moments.getMentioned(), e -> propsFilter(e, member_list_json));
        jsonObject.put("mentions", j2);
        jsonObject.put("createDate", ISO_DATETIME_FORMAT.format(moments.getPublishTime()));
        JSONObject jo = new JSONObject();
        jo.put("huimeiquanData", jsonObject);
        return OK(jo);
    }

    /**
     * 查看点赞列表
     */
    @RequestMapping(value = "/likes", method = GET)
    public Result listLikes(BackPageVo backPageVo, String text, Long id) {
        Page<MomentsFavorites> momentsFavorites = momentsService.listLikes(backPageVo, decodePathVariable(text), id);
        Page<JSONObject> jsonObjects = momentsFavorites.map(e -> {
            JSONObject jsonObject = propsFilter(e, moments_like_json);
            ifNotNullThen(e.getMember().getUsername(), x -> jsonObject.replace("nickName", decodePathVariable(x)));
            jsonObject.put("createTime", ISO_DATETIME_FORMAT.format(e.getFavoriteDate()));
            return jsonObject;
        });
        return OK(jsonObjects);
    }

    /**
     * 6.1.6 查看评论列表
     */
    @RequestMapping(value = "/commentaries", method = GET)
    public Result listCommentaries(BackPageVo backPageVo, String text, Long id) {
        Page<Commentary> commentaries = momentsService.listCommentaries(backPageVo, decodePathVariable(text), id);
        Page<JSONObject> jsonObjects = commentaries.map(e -> {
            JSONObject jsonObject = propsFilter(e, commentary_json);
            ifNotNullThen(e.getCommentator().getUsername(), x -> jsonObject.replace("nickName", decodePathVariable(x)));
            if (e.getState() == NO) {
                jsonObject.put("state", false);
            }
            if (e.getState() == YES) {
                jsonObject.put("state", true);
            }
            if (isNull(e.getParentCommentary())) {
                jsonObject.put("replyNickName", null);
                jsonObject.put("replyUserName", null);
            } else {
                jsonObject.put("replyNickName", e.getParentCommentary().getCommentator().getUsername());
                jsonObject.put("replyUserName", e.getParentCommentary().getCommentator().getRealName());
            }
            jsonObject.put("createTime", ISO_DATETIME_FORMAT.format(e.getCreateTime()));
            return jsonObject;
        });
        return OK(jsonObjects);
    }

    /**
     * 评论启用
     */
    @RequestMapping(value = "/comState", method = PUT)
    public Result comState(Long id) {
        momentsService.comState(id);
        return OK();
    }


}
