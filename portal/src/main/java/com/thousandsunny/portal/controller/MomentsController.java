package com.thousandsunny.portal.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ArrayListMultimap;
import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.ModuleKey.MemberMsgType;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.MemberMsg;
import com.thousandsunny.service.model.Moments;
import com.thousandsunny.service.model.MomentsFavorites;
import com.thousandsunny.service.repository.MomentsRepository;
import com.thousandsunny.service.service.MemberMsgService;
import com.thousandsunny.service.service.MomentsBlockedService;
import com.thousandsunny.service.service.MomentsService;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import static com.thousandsunny.common.HTMLUtil.decodePathVariable;
import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.COMMENT_MOMENTS;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.LIKE_MOMENTS;
import static com.thousandsunny.core.ModuleKey.OK;
import static com.thousandsunny.service.ModuleKey.MomentsType.TELETEXT;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_DATE_FORMAT;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/api/portal/moments", produces = APPLICATION_JSON_UTF8_VALUE)
public class MomentsController {
    private final static String[] moments_user_json = {
            "id",
            "publishTime:date",
            "contentType:type"
    };

    private final static String[] moments_detail_json = {
            "id",
            "publishTime:date",
            "contentType:type",
            "position",

    };
    private final static String[] author_json = {
            "token",
            "headImage.path:headerImageUrl",
            "realName",
            "username:nickName"
    };
    private final static String[] member_json = {
            "token",
            "realName",
            "username:nickName"
    };
    private final static String[] url_json = {
            "path",
    };
    private final static String[] article_json = {
            "id",
            "shop.logo.path:storeImageUrl"
    };
    private final static String[] member_like_json = {
            "member.token:token",
            "member.headImage.path:headerImageUrl",
            "member.realName:realName",
            "member.username:nickName"
    };
    private final static String[] commentary_json = {
            "id",
            "createTime:date",
            "content",
    };
    @Autowired
    private MomentsService momentsService;
    @Autowired
    private MomentsRepository momentsRepository;
    @Autowired
    private MemberMsgService memberMsgService;
    @Autowired
    private MomentsBlockedService momentsBlockedService;

    /**
     * 发表说说
     */
    @RequestMapping(value = "/publish", method = POST)
    public ResponseEntity publishMoments(String userToken, Moments moments,
                                         String[] tokens, String content, Long provinceId, Long cityId, Long areaId) {
        momentsService.publishMoments(userToken, moments,
                isNull(tokens) ? new String[]{} : tokens, TELETEXT, content, provinceId, cityId, areaId);
        return OK;

    }

    /**
     * 删除说说
     */
    @RequestMapping(value = "/delete", method = DELETE)
    public ResponseEntity deleteMoments(String userToken, Long id) {
        momentsService.deleteMoments(userToken, id);
        return OK;
    }

    /**
     * 说说点赞
     */
    @RequestMapping(value = "/favorite", method = POST)
    public ResponseEntity makeMomentsFavorite(String userToken, Long id, OperatorType operatorType) {
        momentsService.makeMomentsFavorites(userToken, id, operatorType);
        return OK;
    }

    /**
     * 发表评论
     */
    @RequestMapping(value = "/comment", method = POST)
    public ResponseEntity comment(String userToken, Long id, Long commentId, String content) {
        Commentary commentary = momentsService.publishComment(userToken, id, commentId, decodePathVariable(content));
        return ok(propsFilter(commentary, "id"));
    }

    /**
     * 删除评论
     */
    @RequestMapping(value = "/deleteCommentary", method = DELETE)
    public ResponseEntity deleteCommentary(String userToken, Long id) {
        momentsService.deleteCommentary(userToken, id);
        return OK;
    }

    /**
     * 用户说说列表
     */
    @RequestMapping(value = "/memberMomentList", method = GET)
    public ResponseEntity listMoment(String userToken, String talkUserToken, PageVO pageVO) {
        Page<Moments> moments = momentsService.findUserPagedMoments(talkUserToken, pageVO.descPageRequest("publishTime"));
        ArrayListMultimap<Date, JSONObject> multimap = ArrayListMultimap.create();
//        TreeSet<Date> treeSet = new TreeSet();
        List<Date> times = new ArrayList<Date>();
        moments.map(e -> {
            JSONObject jo = propsFilter(e, moments_user_json);
            jo.put("content", new String(e.getContent(), Charset.forName("UTF-8")));
            if (e.getPics() != null)
                jo.put("imageCount", e.getPics().size());
            else
                jo.put("imageCount", 0);
            parseMomentsLink(e, jo);
            momentsService.listToJSONObject(e.getPics(), jo, "imageList", url_json);

            List<MomentsFavorites> momentsFavorites = momentsService.getMomentsFavorites(e.getId());
//            momentsService.listToJSONObject(momentsFavorites, jo, "likeList", member_like_json);
            parseMomentsLikeList(jo, momentsFavorites);
            multimap.put(transformDate(e.getPublishTime()), jo);
//            treeSet.add(transformDate(e.getPublishTime()));
            if (!times.contains(transformDate(e.getPublishTime()))) {
                times.add(transformDate(e.getPublishTime()));
            }
            return jo;
        });
        List<JSONObject> jsonObjects = new ArrayList<>();

        times.forEach(e -> {
            JSONObject j = new JSONObject();
            List<JSONObject> jsonObjectList = multimap.get(e);
            if (jsonObjectList != null && jsonObjectList.size() > 0) {
                j.put("date", e);
                j.put("list", multimap.get(e));
                jsonObjects.add(j);
            }

        });
        JSONObject body = new JSONObject();
        body.put("last", !moments.hasNext());
        body.put("first", !moments.hasPrevious());
        body.put("list", jsonObjects);
        body.put("pageNo", pageVO.getPageNo());
        return ok(body);
    }

    private void parseMomentsLikeList(JSONObject jo, List<MomentsFavorites> momentsFavorites) {
        if (momentsFavorites != null) {
            jo.put("likeList", simpleMap(momentsFavorites, t -> {
                JSONObject jsonObject = propsFilter(t, member_like_json);
                ifNotNullThen(t.getMember().getUsername(), m -> jsonObject.replace("nickName", decodePathVariable(m)));
                return jsonObject;
            }));
        } else jo.put("likeList", null);
    }

    public void parseMomentsAuthor(Member member, JSONObject jsonObject, String key, String... props) {
        if (member != null) {
            JSONObject jo = propsFilter(member, props);
            ifNotNullThen(member.getUsername(), x -> jo.replace("nickName", decodePathVariable(x)));
            jsonObject.put(key, jo);
        } else jsonObject.put(key, null);
    }

    private JSONObject parseMomentsLink(Moments moments, JSONObject body) {
        if (moments.getJob() != null) {
            JSONObject jo = propsFilter(moments.getJob(), article_json);
            jo.put("title", new String(moments.getContent(), Charset.forName("UTF-8")));
            body.put("link", jo);
        } else body.put("link", null);
        return body;
    }

    private void parseMomentsAtList(List<Member> list, JSONObject jsonObject, String key) {
        if (list != null)
            jsonObject.put(key, simpleMap(list, e -> {
                JSONObject jo = propsFilter(e, member_json);
                ifNotNullThen(e.getUsername(), x -> jo.replace("nickName", decodePathVariable(x)));
                return jo;
            }));
        else
            jsonObject.put(key, null);
    }

    private Date transformDate(Date date) {
        try {
            return ISO_DATE_FORMAT.parse(ISO_DATE_FORMAT.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 说说详情
     */
    @RequestMapping(value = "/detail", method = GET)
    public ResponseEntity showMomentsDetails(String userToken, Long id) {
        Moments moments = momentsRepository.findOne(id);
        JSONObject jsonObject = propsFilter(moments, moments_detail_json);
        jsonObject.put("content", new String(moments.getContent(), Charset.forName("UTF-8")));
        Boolean isLike = momentsService.isFavorite(userToken, id);
        jsonObject.put("isLike", isLike);
//        momentsService.objectToJSONObject(moments.getAuthor(), jsonObject, "authorUser", author_json);
        parseMomentsAuthor(moments.getAuthor(), jsonObject, "authorUser", author_json);
//        momentsService.objectToJSONObject(moments.getShop(), jsonObject, "link", article_json);
        parseMomentsLink(moments, jsonObject);
        momentsService.listToJSONObject(moments.getPics(), jsonObject, "imageList", url_json);
//        momentsService.listToJSONObject(moments.getMentioned(), jsonObject, "atList", member_json);
        parseMomentsAtList(moments.getMentioned(), jsonObject, "atList");
        List<MomentsFavorites> momentsFavorites = momentsService.getMomentsFavorites(id);
//        momentsService.listToJSONObject(momentsFavorites, jsonObject, "likeList", member_like_json);
        parseMomentsLikeList(jsonObject, momentsFavorites);

        jsonObject.put("commentList", wrapCommentary(momentsService.getCommentaries(id)));
        return ok(jsonObject);
    }

    /**
     * 说说列表
     */
    @RequestMapping(value = "/list", method = GET)
    public ResponseEntity lBooleanistMoments(String userToken, PageVO pageVO, String provinceId, String cityId, String areaId) {
        List<Moments> momentses = momentsService.findNearbyMoments(pageVO.pageRequest(), provinceId, cityId, areaId);
        List<JSONObject> jsonObjects = simpleFilterMap(momentses, e -> {
            if (isBlank(userToken)) return true;
            else {
                boolean flag = momentsBlockedService.findByMemberTokenAndMomentsMemberToken(userToken, e.getAuthor().getToken());
                return !flag;
            }
        }, e -> {
            JSONObject jo = propsFilter(e, moments_detail_json);
            if (e.getContent() != null) {
                jo.put("content", new String(e.getContent(), Charset.forName("UTF-8")));
            } else {
                jo.put("content", "");
            }
            Boolean isLike = momentsService.isFavorite(userToken, e.getId());
            jo.put("isLike", isLike);
//            momentsService.objectToJSONObject(e.getAuthor(), jo, "authorUser", author_json);
            parseMomentsAuthor(e.getAuthor(), jo, "authorUser", author_json);
//            momentsService.objectToJSONObject(e.getShop(), jo, "link", article_json);
            parseMomentsLink(e, jo);
            momentsService.listToJSONObject(e.getPics(), jo, "imageList", url_json);
//            momentsService.listToJSONObject(e.getMentioned(), jo, "atList", member_json);
            parseMomentsAtList(e.getMentioned(), jo, "atList");
            List<MomentsFavorites> momentsFavorites = momentsService.getMomentsFavorites(e.getId());
//            momentsService.listToJSONObject(momentsFavorites, jo, "likeList", member_like_json);
            parseMomentsLikeList(jo, momentsFavorites);
            jo.put("commentList", wrapCommentary(momentsService.getCommentaries(e.getId())));
            return jo;
        });
        JSONObject j = new JSONObject();
        j.put("list", jsonObjects);
        j.put("last", momentses.size() != pageVO.getPageSize());
        j.put("pageNo", pageVO.getPageNo());
        return ok(j);
    }

    private List<JSONObject> wrapCommentary(List<Commentary> commentaries) {
        return simpleMap(commentaries, getCommentaryJSONObjectFunction());
    }

    private Function<Commentary, JSONObject> getCommentaryJSONObjectFunction() {
        return x -> {
            JSONObject j = propsFilter(x, commentary_json);
//            JSONObject j1 = propsFilter(x.getCommentator(), author_json);
//            j.put("commentUser", j1);
            parseMomentsAuthor(x.getCommentator(), j, "commentUser", author_json);
            if (x.getParentCommentary() != null) {
                momentsService.objectToJSONObject(x.getParentCommentary().getCommentator(), j, "replyUser", member_json);
                parseMomentsAuthor(x.getParentCommentary().getCommentator(), j, "replyUser", member_json);
            } else j.put("replyUser", null);
            return j;
        };
    }

    /**
     * 说说图片收藏
     */
    @RequestMapping(value = "/pictureCollect", method = POST)
    public ResponseEntity collectPicture(String userToken, String key, String picUserToken, OperatorType operatorType) {
        momentsService.collectPicture(userToken, key, picUserToken, operatorType);
        return OK;
    }


    /**
     * 判断说说图片是否收藏
     */
    @RequestMapping(value = "/isCollected", method = GET)
    public ResponseEntity isPictureCollected(String userToken, String key, String picUserToken) {
        momentsService.isPictureCollected(userToken, key, picUserToken);
        return OK;
    }


    /**
     * 4.11说说最新消息
     */
    @RequestMapping(value = "/lastMessage", method = GET)
    public ResponseEntity lastMessage(String userToken) {
        List<MemberMsg> memberMsgs = momentsService.lastMessage(userToken);
        Member last = null;
        JSONObject jsonObject = new JSONObject();
        if (memberMsgs != null && memberMsgs.size() > 0) {
            MemberMsg memberMsg = memberMsgs.get(0);
            MomentsFavorites mf = memberMsg.getMomentsFavorites();
            Commentary ct = memberMsg.getCommentary();
            if (mf != null) {
                last = mf.getMember();
            }
            if (ct != null) {
                last = ct.getCommentator();
            }
            jsonObject.put("count", memberMsgs.size());
            if (last != null) {
                CloudFile headImage = last.getHeadImage();
                String latestHeaderImageUrl = headImage == null ? "" : headImage.getPath();
                jsonObject.put("latestHeaderImageUrl", latestHeaderImageUrl);
            }
        } else {
            jsonObject.put("count", 0);
        }
        return ok(jsonObject);
    }


    /**
     * 4.12说说消息列表
     */
    @RequestMapping(value = "/messageList", method = GET)
    public ResponseEntity messageList(String userToken, PageVO pageVO) {
        Page<MemberMsg> memberMsgs = momentsService.messageList(userToken, pageVO.pageRequest());
        List<MemberMsg> memberMsgList = momentsService.messageList(userToken);
        memberMsgService.isReading(memberMsgList);
        JSONObject jsonObject = pageToJson(memberMsgs, e -> {
            MemberMsgType type = e.getType();
            Moments moments = e.getMoments();
            List<CloudFile> cloudFiles = moments.getPics();
            JSONObject jo = new JSONObject();
            jo.put("id", e.getId());
            jo.put("talkContent", new String(moments.getContent(), Charset.forName("UTF-8")));
            jo.put("date", e.getDate());
            jo.put("talkType", moments.getContentType());
            ifNotEmptyThen(cloudFiles, c -> jo.put("talkFirstImageUrl", c.get(0).getPath()));
            if (type == COMMENT_MOMENTS) {
                jo.put("type", "COMMENT");
                jo.put("content", e.getCommentary().getContent());
//                momentsService.objectToJSONObject(e.getCommentary().getCommentator(), jo, "user", author_json);
                parseMomentsAuthor(e.getCommentary().getCommentator(), jo, "user", author_json);
                if (e.getCommentary().getParentCommentary() != null) {
//                    momentsService.objectToJSONObject(e.getCommentary().getParentCommentary().getCommentator(), jo, "replyUser", author_json);
                    parseMomentsAuthor(e.getCommentary().getParentCommentary().getCommentator(), jo, "replyUser", author_json);
                } else {
                    jo.put("replyUser", null);
                }
            }
            if (type == LIKE_MOMENTS) {
                jo.put("type", "LIKE");
                Member like = e.getMomentsFavorites().getMember();
//                momentsService.objectToJSONObject(like, jo, "user", author_json);
                parseMomentsAuthor(like, jo, "user", author_json);
            }
            return jo;
        });
        return ok(jsonObject);
    }

    /**
     * 4.13说说消息列表-清空消息
     */
    @RequestMapping(value = "/emptyMessage", method = POST)
    public ResponseEntity emptyMessage(String userToken, Long startMessageId) {
        memberMsgService.emptyMessage(userToken, startMessageId);
        return OK;
    }


    /**
     * 4.14说说消息列表-删除
     */
    @RequestMapping(value = "/delMessage", method = POST)
    public ResponseEntity delMessage(String userToken, Long id) {
        memberMsgService.delMessage(userToken, id);
        return OK;
    }

}
