package com.thousandsunny.service.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.thousandsunny.cms.domain.repository.CommentaryRepository;
import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.common.HTMLUtil;
import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.common.lambda.LambdaUtil;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.repository.FriendsRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.repository.RegionRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.MomentsType;
import com.thousandsunny.service.ModuleKey.StateEnum;
import com.thousandsunny.service.model.MemberMsg;
import com.thousandsunny.service.model.Moments;
import com.thousandsunny.service.model.MomentsFavorites;
import com.thousandsunny.service.model.PictureCollect;
import com.thousandsunny.service.repository.MemberMsgRepository;
import com.thousandsunny.service.repository.MomentsFavoritesRepository;
import com.thousandsunny.service.repository.MomentsRepository;
import com.thousandsunny.service.repository.PictureCollectRepository;
import com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import org.hibernate.jpa.criteria.OrderImpl;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import javax.persistence.criteria.Predicate;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.thousandsunny.common.JsonUtil.propsFilter;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.FileType.IMAGE;
import static com.thousandsunny.core.ModuleKey.MemberMsgType;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.*;
import static com.thousandsunny.service.ModuleKey.StateEnum.HIDE;
import static com.thousandsunny.service.ModuleTips.*;
import static java.util.Objects.deepEquals;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;

@Service
public class MomentsService extends BaseService<Moments> {
    @Autowired
    private MomentsRepository momentsRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;
    @Autowired
    private MomentsFavoritesRepository momentsFavoritesRepository;
    @Autowired
    private CommentaryRepository commentaryRepository;
    @Autowired
    private PictureCollectRepository pictureCollectRepository;
    @Autowired
    private MemberMsgRepository memberMsgRepository;
    @Autowired
    private MemberMsgService memberMsgService;
    @Autowired
    private RegionRepository regionRepository;

    /**
     * 发布说说
     */
    public void publishMoments(String userToken, Moments moments, String[] tokens,
                               MomentsType type, String content, Long provinceId, Long cityId, Long areaId) {
        moments.setPosition(HTMLUtil.decodePathVariable(moments.getPosition()));
        moments.setContent(content.getBytes(Charset.forName("UTF-8")));
        ifNotNullThen(provinceId, e -> moments.setProvince(regionRepository.findByIdAndRegionLevel(e, 1)));
        ifNotNullThen(cityId, e -> moments.setCity(regionRepository.findByIdAndRegionLevel(e, 2)));
        ifNotNullThen(areaId, e -> moments.setArea(regionRepository.findByIdAndRegionLevel(e, 3)));
        Member publisher = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(publisher, TIP_NO_MEMBER);
        moments.setContentType(type);
        moments.setAuthor(publisher);
        List<Member> members = Stream.of(tokens)
                .map(token -> memberRepository.findByTokenAndIsDelete(token, NO))
                .filter(LambdaUtil::isNotNull)
                .collect(toList());
        moments.setMentioned(members);
        ifNotNullThen(moments.getPics(), l -> l.forEach(f -> f.setType(IMAGE)));
        moments.setPics(cloudFileRepository.save(moments.getPics()));
        momentsRepository.save(moments);
        sendMessage(moments);
    }

    private void sendMessage(Moments moments) {
        if (!moments.getMentioned().isEmpty()) {
            moments.getMentioned().forEach(e -> {
                memberMsgService.updateMemberMsgIsNew(e, TALK_AT_REMIND);
                MemberMsg memberMsg = new MemberMsg();
                memberMsg.setMoments(moments);
                memberMsg.setReceiver(e);
                memberMsg.setContent("你的好友" + moments.getAuthor().getRealName() + "在慧美圈中提到了你，去查看！");
                memberMsg.setType(TALK_AT_REMIND);
                memberMsgRepository.save(memberMsg);
            });
        }
    }

    public void deleteMoments(String userToken, Long id) {
        Moments moments = momentsRepository.findByAuthorTokenAndIdAndIsDelete(userToken, id, StateEnum.NO);
        ifNullThrow(moments, TIP_NO_COMMENTS);
        moments.setIsDelete(StateEnum.YES);
    }

    public void makeMomentsFavorites(String userToken, Long id, OperatorType operatorType) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        Moments moments = momentsRepository.findOne(id);
        ifNullThrow(moments, TIP_NO_COMMENTS);
        MomentsFavorites momentsFavorites = momentsFavoritesRepository.findByMemberTokenAndMomentsId(userToken, id);
        if (operatorType == OperatorType.SURE) {
            ifTrueThrow(momentsFavorites != null && momentsFavorites.getFavoriteEver() == NO, TIP_FAVORITE);
            if (momentsFavorites == null) {
                momentsFavorites = new MomentsFavorites();
                momentsFavorites.setMember(member);
                momentsFavorites.setMoments(moments);
                momentsFavorites.setFavoriteDate(new Date());
                momentsFavoritesRepository.save(momentsFavorites);
                if (!moments.getAuthor().equals(member)) {
                    sendMessageLike(moments.getAuthor(), moments, momentsFavorites);
                }

//                Set<Member> receives = new HashSet<>();
//                receives.add(moments.getAuthor());
//                List<MomentsFavorites> mfs = moments.getMemberFavorites();
//                List<Commentary> commentaryList = moments.getCommentaries();
//                for(MomentsFavorites mf:mfs){
//                    if(mf.getFavoriteEver()==NO){
//                        receives.add(mf.getMember());
//                    }
//                }
//
//                if(commentaryList!=null){
//                    for(Commentary commentary:commentaryList){
//                        Member user = commentary.getCommentator();
//                        Member replyUser = null;
//                        Commentary replyCommentary = commentary.getParentCommentary();
//                        if(replyCommentary!=null){
//                            replyUser = replyCommentary.getCommentator();
//                        }
//                        receives.add(user);
//                        if(replyUser!=null){
//                            receives.add(replyUser);
//                        }
//
//                    }
//                }
//
//                for(Member m:receives){
//                    Friends f1 = friendsRepository.findByOwnerTokenAndFriendToken(m.getToken(),member.getToken());
//                    Friends f2 = friendsRepository.findByOwnerTokenAndFriendToken(member.getToken(),m.getToken());
//                    if(f1!=null||f2!=null){
//                        if(m!=member){
//                            sendMessageLike(m,moments,momentsFavorites);
//                        }
//
//                    }
//
//                }

            } else {
                momentsFavorites.setFavoriteEver(NO);
                momentsFavorites.setFavoriteDate(new Date());
            }
        } else {
            ifNullThrow(momentsFavorites, TIP_NOT_FAVORITE);
            ifTrueThrow(momentsFavorites != null && momentsFavorites.getFavoriteEver() == YES, TIP_CANCEL_FAVORITE);
            momentsFavorites.setFavoriteEver(YES);
        }
    }

    private void sendMessageLike(Member receiver, Moments moments, MomentsFavorites momentsFavorites) {
        memberMsgService.updateMemberMsgIsNew(receiver, COMMENT_MOMENTS);
        memberMsgService.updateMemberMsgIsNew(receiver, LIKE_MOMENTS);
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setReceiver(receiver);
        memberMsg.setType(LIKE_MOMENTS);
        memberMsg.setMoments(moments);
        memberMsg.setMomentsFavorites(momentsFavorites);
        memberMsg.setContent("你的好友" + momentsFavorites.getMember().getRealName() + "在慧美圈对你进行点赞！");
        memberMsg.setDate(new Date());
        memberMsgRepository.save(memberMsg);
    }

    public Commentary publishComment(String userToken, Long id, Long commentId, String content) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        Moments moments = momentsRepository.findOne(id);
        ifNullThrow(moments, TIP_NO_COMMENTS);
        Set<Member> receivers = new HashSet<>();
        receivers.add(moments.getAuthor());
        Commentary commentary = new Commentary();
        commentary.setMoments(moments);
        commentary.setCommentator(member);
        commentary.setContent(content);
        if (commentId != null) {
            Commentary parentCommentary = commentaryRepository.findOne(commentId);
            ifNullThrow(parentCommentary, TIP_NO_COMMENTARY);
            commentary.setParentCommentary(parentCommentary);
            Member reviewer = parentCommentary.getCommentator();
            receivers.add(reviewer);
        }

        Commentary c = commentaryRepository.save(commentary);
        for (Member m : receivers) {
            if (!m.equals(member)) {
                sendMessage(m, moments, commentary);
            }
        }
        return c;
    }

//    public Commentary publishComment(String userToken, Long id, Long commentId, String content) {
//        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
//        ifNullThrow(member, TIP_NO_MEMBER);
//        Moments moments = momentsRepository.findOne(id);
//        List<MomentsFavorites> mfs= moments.getMemberFavorites();
//        List<Commentary> commentaryList = moments.getCommentaries();
//        ifNullThrow(moments, TIP_NO_COMMENTS);
//        Commentary commentary = new Commentary();
//        commentary.setMoments(moments);
//        commentary.setCommentator(member);
//        commentary.setContent(content);
//        Set<Member> receives = new HashSet<>();
//        receives.add(moments.getAuthor());
//        if(commentaryList!=null&&commentaryList.size()>0){
//            for(Commentary ct:commentaryList){
//                Member user = ct.getCommentator();
//                Member replyUser = null;
//                Commentary replyCommentary = ct.getParentCommentary();
//                if(replyCommentary!=null){
//                    replyUser = replyCommentary.getCommentator();
//                }
//                receives.add(user);
//                if(replyUser!=null){
//                    receives.add(replyUser);
//                }
//
//            }
//        }
//
//        if (commentId != null) {
//            Commentary parentCommentary = commentaryRepository.findOne(commentId);
//            ifNullThrow(parentCommentary, TIP_NO_COMMENTARY);
//            commentary.setParentCommentary(parentCommentary);
//        }
//
//        if(mfs!=null){
//            for(MomentsFavorites mf:mfs){
//                if(mf.getFavoriteEver()==NO){
//                    receives.add(mf.getMember());
//                }
//            }
//        }
//
//        Commentary c = commentaryRepository.save(commentary);
//        for(Member m:receives){
//            Friends f1 = friendsRepository.findByOwnerTokenAndFriendToken(m.getToken(),member.getToken());
//            Friends f2 = friendsRepository.findByOwnerTokenAndFriendToken(member.getToken(),m.getToken());
//            if(f1!=null||f2!=null){
//                if(m!=member){
//                    senMessage(m,moments,c);
//                }
//            }
//
//        }
//        return c;
//    }

    private void sendMessage(Member receiver, Moments moments, Commentary c) {
        memberMsgService.updateMemberMsgIsNew(receiver, COMMENT_MOMENTS);
        memberMsgService.updateMemberMsgIsNew(receiver, LIKE_MOMENTS);
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setReceiver(receiver);
        memberMsg.setType(COMMENT_MOMENTS);
        memberMsg.setMoments(moments);
        memberMsg.setCommentary(c);
        memberMsg.setContent("你的好友" + c.getCommentator().getRealName() + "在慧美圈对你进行评论！");
        memberMsg.setDate(new Date());
        memberMsgRepository.save(memberMsg);
    }

    public void deleteCommentary(String userToken, Long id) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        Commentary commentary = commentaryRepository.findOne(id);
        ifNullThrow(commentary, TIP_NO_COMMENTARY);
        ifFalseThrow(deepEquals(member.getId(), commentary.getCommentator().getId()), TIP_NO_AUTHORITY);
        List<Commentary> commentaries = commentary.getChildCommentaries();
        commentaries.forEach(e -> e.setParentCommentary(null));
        List<MemberMsg> memberMsgs = memberMsgRepository.findByCommentary(commentary);
        memberMsgRepository.delete(memberMsgs);
        commentaryRepository.delete(commentary);
    }

    public Page<Moments> findUserPagedMoments(String userToken, Pageable pageable) {
        return momentsRepository.findByAuthorTokenAndIsDeleteOrderByPublishTimeDesc(userToken, StateEnum.NO, pageable);
    }

    public Boolean isFavorite(String userToken, Long momentsId) {
        MomentsFavorites momentsFavorites = momentsFavoritesRepository.findByMemberTokenAndMomentsId(userToken, momentsId);
        return momentsFavorites != null && momentsFavorites.getFavoriteEver() == NO;
    }

    public List<MomentsFavorites> getMomentsFavorites(Long momentsId) {
        return momentsFavoritesRepository.findByMomentsIdAndFavoriteEver(momentsId, NO);
    }

    public void listToJSONObject(List list, JSONObject jsonObject, String key, String... props) {
        if (list != null)
            jsonObject.put(key, simpleMap(list, e -> propsFilter(e, props)));
        else
            jsonObject.put(key, null);
    }

    public List<Commentary> getCommentaries(Long id) {
        return commentaryRepository.findByMomentsId(id);
    }

    public void objectToJSONObject(Object object, JSONObject jsonObject, String key, String... props) {
        if (object != null) {
            JSONObject jo = propsFilter(object, props);
            jsonObject.put(key, jo);
        } else jsonObject.put(key, null);
    }

    public void collectPicture(String userToken, String key, String picUserToken, OperatorType operatorType) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        Member owner = memberRepository.findByTokenAndIsDelete(picUserToken, NO);
        ifNullThrow(owner, TIP_NO_MEMBER);
        CloudFile picture = cloudFileRepository.findTop1ByPath(key);
        ifNullThrow(picture, TIP_NO_PICTURE);
        PictureCollect pictureCollect = pictureCollectRepository.findByMemberTokenAndOwnerTokenAndPicturePath(userToken, picUserToken, key);
        if (operatorType == OperatorType.SURE) {
            ifTrueThrow((!isNull(pictureCollect)) && pictureCollect.getCollectEver() == NO, TIP_COLLECT);
            if (isNull(pictureCollect)) {
                pictureCollect = new PictureCollect();
                pictureCollect.setMember(member);
                pictureCollect.setOwner(owner);
                pictureCollect.setPicture(picture);
                pictureCollectRepository.save(pictureCollect);
            } else {
                pictureCollect.setCollectEver(NO);
                pictureCollect.setDate(new Date());
            }

        } else {
            ifNullThrow(pictureCollect, TIP_NOT_COLLECT);
            ifTrueThrow(pictureCollect.getCollectEver() == YES, TIP_CANCEL_COLLECT);
            pictureCollect.setCollectEver(YES);
        }
    }

    public void isPictureCollected(String userToken, String key, String picUserToken) {
        PictureCollect pictureCollect = pictureCollectRepository.findByMemberTokenAndOwnerTokenAndPicturePath(userToken, picUserToken, key);
        ifTrueThrow((!isNull(pictureCollect)) && pictureCollect.getCollectEver() == NO, TIP_COLLECT);
    }

    public List<Moments> findNearbyMoments(Pageable pageabel, String provinceId, String cityId, String areaId) {

        StringBuffer sql = new StringBuffer();
        sql.append("select * from sr_moments m where is_delete = 'NO'");
        if (!"".equals(provinceId) && provinceId != null) {
            sql.append(" and province_id ='" + provinceId + "'");
        }
        if (!"".equals(cityId) && cityId != null) {
            sql.append(" and city_id ='" + cityId + "'");
        }
        if (!"".equals(areaId) && areaId != null) {
            sql.append(" and area_id ='" + areaId + "'");
        }

//        if (longitude != null && latitude != null) {
//            sql.append("order by round(6378.138*2*asin(sqrt(pow(sin((" + latitude + "*pi()/180-m.latitude*pi()/180)/2),2)+" +
//                    "cos(" + latitude + "*pi()/180)*cos(m.latitude*pi()/180)*" +
//                    "pow(sin((" + longitude + "*pi()/180-m.longitude*pi()/180)/2),2)))*1000),m.publish_time DESC limit " + pageabel.getOffset() + "," + pageabel.getPageSize());
//
//        } else {
        sql.append("order by publish_time DESC limit " + pageabel.getOffset() + "," + pageabel.getPageSize());
//        }
        Query query = entityManager.createNativeQuery(sql.toString(), Moments.class);
        List<Moments> momentses = query.getResultList();
        return momentses;
    }

    public Page<Moments> momentses(BackPageVo backPageVo, String text, Long provinceId, Long cityId, Long areaId) {
        Specification<Moments> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            //TODO:xiaoxuewei 匹配惠美圈内容待定
            ifNotBlankThen(text, e -> predicates.add(rb.or(rb.like(rt.get("author").get("mobile"), "%" + e + "%"),
                    rb.like(rt.get("author").get("hpAccount"), "%" + e + "%"),
                    rb.like(rt.get("author").get("realName"), "%" + e + "%"))));
            predicates.add(rb.or(rb.equal(rt.get("isDelete"), StateEnum.NO), rb.equal(rt.get("isDelete"), HIDE)));
            ifNotNullThen(provinceId, e -> predicates.add(rb.equal(rt.get("province").get("id"), e)));
            ifNotNullThen(cityId, e -> predicates.add(rb.equal(rt.get("city").get("id"), e)));
            ifNotNullThen(areaId, e -> predicates.add(rb.equal(rt.get("area").get("id"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("publishTime"), false)).getRestriction();
        };
        return momentsRepository.findAll(specification, backPageVo.pageRequest());
    }

    public Integer countLikers(Long id) {
        return momentsFavoritesRepository.countByMomentsIdAndFavoriteEver(id, NO);
    }

    public Integer countComments(Long id) {
        return commentaryRepository.countByMomentsId(id);
    }

    public void delete(String s) {
        String[] ids = s.split(",");
        Lists.newArrayList(ids).forEach(e -> {
            Moments moments = momentsRepository.findOne(Long.valueOf(e));
            moments.setIsDelete(StateEnum.YES);
            momentsRepository.save(moments);
        });
    }

    public void state(Long id) {
        Moments moments = momentsRepository.findOne(id);
        if (moments.getIsDelete() == StateEnum.NO)
            moments.setIsDelete(HIDE);
        else
            moments.setIsDelete(StateEnum.NO);
    }

    public Page<MomentsFavorites> listLikes(BackPageVo backPageVo, String text, Long id) {
        Specification<MomentsFavorites> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            predicates.add(rb.equal(rt.get("moments").get("id"), id));
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("member").get("mobile"), "%" + t + "%"), rb.or(rb.like(rt.get("member").get("realName"), "%" + t + "%"), rb.like(rt.get("member").get("hpAccount"), "%" + t + "%")))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("favoriteDate"), false)).getRestriction();
        };
        return momentsFavoritesRepository.findAll(specification, backPageVo.pageRequest());
    }

    public Page<Commentary> listCommentaries(BackPageVo backPageVo, String text, Long id) {
        Specification<Commentary> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            predicates.add(rb.equal(rt.get("moments"), getMomentById(id)));
            ifNotNullThen(text, t -> predicates.add(rb.or(rb.like(rt.get("content"), "%" + t + "%"), rb.like(rt.get("commentator").get("realName"), "%" + t + "%"))));
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("createTime"), false)).getRestriction();
        };
        return commentaryRepository.findAll(specification, backPageVo.pageRequest());
    }

    public Moments getMomentById(Long id) {
        List<StateEnum> states = Lists.newArrayList(StateEnum.NO, HIDE);
        Moments byIdAndIsDeleteIn = momentsRepository.findByIdAndIsDeleteIn(id, states);
        return byIdAndIsDeleteIn;
    }

    public void comState(Long id) {
        Commentary commentary = commentaryRepository.findOne(id);
        if (commentary.getState() == NO)
            commentary.setState(YES);
        else
            commentary.setState(NO);
    }


    public List<MemberMsg> lastMessage(String userToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<MemberMsgType> memberMsgTypes = newArrayList(COMMENT_MOMENTS, LIKE_MOMENTS);
        List<MemberMsg> memberMsgs = memberMsgRepository.findByReceiverAndIsDeleteAndTypeInAndIsReadOrderByDateDesc(member, NO, memberMsgTypes, NO);
        return memberMsgs;
    }

    public Page<MemberMsg> messageList(String userToken, Pageable pageable) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<MemberMsgType> memberMsgTypes = newArrayList(COMMENT_MOMENTS, LIKE_MOMENTS);
        Page<MemberMsg> memberMsgs = memberMsgRepository.findByReceiverAndIsDeleteAndTypeInAndIsReadOrderByDateDesc(member, NO, memberMsgTypes, NO, pageable);
        return memberMsgs;
    }


    public List<MemberMsg> messageList(String userToken) {
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<MemberMsgType> memberMsgTypes = newArrayList(COMMENT_MOMENTS, LIKE_MOMENTS);
        List<MemberMsg> memberMsgs = memberMsgRepository.findByReceiverAndIsDeleteAndTypeInAndIsReadOrderByDateDesc(member, NO, memberMsgTypes, NO);
        return memberMsgs;
    }

    public Page<Moments> findMomentPage(BackPageVo pageVo, String text, Long userId) {
        Specification<Moments> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            predicates.add(rb.equal(rt.get("author").get("id"), userId));
            ifNotBlankThen(text, e -> {
                String textStr = "%" + e + "%";
                predicates.add(rb.like(rt.get("content"), textStr));
            });
            return rq.where(predicates.toArray(new Predicate[]{})).orderBy(new OrderImpl(rt.get("publishTime"), false)).getRestriction();
        };
        return momentsRepository.findAll(spec, pageVo.pageRequest());
    }

    public JSONObject countMomentsInfo(Date startTime, Date endTime) {
        JSONObject jo = new JSONObject();
        Specification<Moments> specification = (rt, rq, rb) -> {
            List<Predicate> predicates = Lists.newArrayList();
            ifNotNullThen(startTime, e -> predicates.add(rb.greaterThan(rt.get("publishTime"), e)));
            ifNotNullThen(endTime, e -> predicates.add(rb.lessThan(rt.get("publishTime"), e)));
            return rq.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };
        List<Moments> list = momentsRepository.findAll(specification);
        jo.put("pubNum", list.size());
        Set<Long> momentsMemberids = new HashSet<>();
        if (!list.isEmpty()) {
            list.forEach(e -> ifNotNullThen(e.getAuthor(), f -> momentsMemberids.add(f.getId())));
            jo.put("pubUserNum", momentsMemberids.size());
        } else
            jo.put("pubUserNum", 0);
        return jo;
    }

}