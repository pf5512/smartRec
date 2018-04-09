package com.thousandsunny.service.service;

import com.thousandsunny.cms.model.Article;
import com.thousandsunny.cms.model.ArticleRead;
import com.thousandsunny.core.ModuleKey.*;
import com.thousandsunny.core.domain.repository.FriendApplyRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseFriendsService;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.FriendApply;
import com.thousandsunny.service.model.MemberMsg;
import com.thousandsunny.service.repository.ArticleReadRepository;
import com.thousandsunny.service.repository.MemberMsgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.*;

import static com.thousandsunny.core.ModuleKey.MemberMsgType.H_FRIEND_APPLY_REMIND;
import static com.thousandsunny.service.ModuleTips.*;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;

/**
 * Created by admin on 2016/11/7.
 */
@Service
public class MemberMsgService extends BaseService<MemberMsg> {
    @Autowired
    private MemberMsgRepository memberMsgRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BaseFriendsService baseFriendsService;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleReadRepository articleReadRepository;

    private List<MemberMsgType> memberMsgTypes = newArrayList(TALK_AT_REMIND, JOB_PREPAY_REMIND, JOB_CHARGEBACK_REMIND, JOB_DEFAULT_REMIND,
            FRIEND_WORK_REMIND, WORK_STATE_CONFIRM_REMIND, H_FRIEND_APPLY_REMIND);

    public List<MemberMsg> getMemberMsgs(String userToken) {
        List<MemberMsg> msgs = memberMsgRepository.findByReceiverTokenAndIsNewAndIsDeleteAndTypeInOrderByDateDesc(userToken, YES, NO, memberMsgTypes);
        return msgs;
    }

    public Page<MemberMsg> getMemberMsgsByType(String token, MemberMsgType type, Pageable pageable) {
        Member member = memberRepository.findByToken(token);
        List<MemberMsg> memberMsgs = memberMsgRepository.findByReceiverAndIsDeleteAndTypeOrderByDateDesc(member, NO, type);
        for (MemberMsg memberMsg : memberMsgs) {
            memberMsg.setIsRead(YES);
        }
        return memberMsgRepository.findByReceiverTokenAndIsDeleteAndTypeOrderByDateDesc(token, NO, type, pageable);
    }

    public Map<String, Long> isNotReadMessageAcount(String userToken) {
        Map<String, Long> map = new HashMap<>();
        Member receiver = memberRepository.findByToken(userToken);
        Long talkCount = memberMsgRepository.countByReceiverAndIsDeleteAndIsReadAndType(receiver, NO, NO, TALK_AT_REMIND);
        Long cooperateCount = memberMsgRepository.countByReceiverAndIsDeleteAndIsReadAndType(receiver, NO, NO, READ_COOPERATE);
        Long transferCount = memberMsgRepository.countByReceiverAndIsDeleteAndIsReadAndType(receiver, NO, NO, READ_TRANSFER);
        Long cardAndTicketCount = memberMsgRepository.countByReceiverAndIsDeleteAndIsReadAndType(receiver, NO, NO, READ_CARD_AND_COUPON);
        Long platformActivityCount = memberMsgRepository.countByReceiverAndIsDeleteAndIsReadAndType(receiver, NO, NO, READ_PLATFORM_ACTIVITY);
        map.put("talkCount", talkCount);
        map.put("cooperateCount", cooperateCount);
        map.put("transferCount", transferCount);
        map.put("cardAndTicketCount", cardAndTicketCount);
        map.put("platformActivityCount", platformActivityCount);
        return map;
    }

    public void oneKeyReading(String userToken, MemberMsgType type) {
        Member member = memberRepository.findByToken(userToken);
        List<MemberMsg> memberMsgs = memberMsgRepository.findByReceiverAndIsDeleteAndTypeOrderByDateDesc(member, NO, type);
        for (MemberMsg memberMsg : memberMsgs) {
            memberMsg.setIsRead(YES);
        }
        memberMsgRepository.save(memberMsgs);
        if (type == READ_PLATFORM_ACTIVITY) {
            Set<Long> readedArticleIds = articleService.findMyReadedArticles(member, 20L);
            Set<Long> platformActivityChannelIds = articleService.findChannelIds(20L);
            platformActivityChannelIds.add(20L);
            List<Article> needReadArticles = articleService.findNeedReadArticles(readedArticleIds, platformActivityChannelIds);
            if (!needReadArticles.isEmpty()) needReadArticles.forEach(article -> {
                ArticleRead articleRead = new ArticleRead();
                articleRead.setMember(member);
                articleRead.setArticle(article);
                articleReadRepository.save(articleRead);
            });
        }
    }

    public void isReading(List<MemberMsg> memberMsgs) {
        for (MemberMsg memberMsg : memberMsgs) {
            memberMsg.setIsRead(YES);
        }
        memberMsgRepository.save(memberMsgs);
    }

    public String addFriend(String userToken, String invitedUserToken) {
        FriendApply friendApply = baseFriendsService.addFriend(userToken, invitedUserToken);
        Member receiver = friendApply.getApprover();
        updateMemberMsgIsNew(receiver, H_FRIEND_APPLY_REMIND);
        MemberMsg memberMsg = new MemberMsg();
        memberMsg.setFriendApply(friendApply);
        memberMsg.setReceiver(receiver);
        memberMsg.setType(H_FRIEND_APPLY_REMIND);
        memberMsg.setContent("有新的慧友请求！");
        memberMsgRepository.save(memberMsg);
        return "success";
    }

    public String acceptApply(String userToken, Long applyId) {

//        FriendApply friendApply = friendApplyRepository.findOne(applyId);
//        ifNullThrow(friendApply, TIP_NO_SUBMIT);
//        baseFriendsService.acceptApply(userToken, friendApply);
//        MemberMsg memberMsg= memberMsgRepository.findByReceiverTokenAndIsDeleteAndFriendApply(userToken,NO,friendApply);
//        if(memberMsg!=null){
//            memberMsg.setIsRead(YES);
//            memberMsgRepository.save(memberMsg);
//        }
//        return "success";
        MemberMsg memberMsg = memberMsgRepository.findOne(applyId);
        ifNullThrow(memberMsg, TIP_NO_SUBMIT);
        FriendApply friendApply = memberMsg.getFriendApply();
        baseFriendsService.acceptApply(userToken, friendApply);
        memberMsg.setIsRead(YES);
        memberMsgRepository.save(memberMsg);
        return "success";
    }

    public void updateMemberMsgIsNew(Member member, MemberMsgType type) {
        List<MemberMsg> memberMsgs = memberMsgRepository.findByReceiverAndIsDeleteAndIsNewAndType(member, NO, YES, type);
        ifNotNullThen(memberMsgs, x -> x.forEach(m -> m.setIsNew(NO)));
        memberMsgRepository.save(memberMsgs);

    }

    public void emptyMessage(String userToken, Long startMessageId) {
        List<MemberMsgType> memberMsgTypes = newArrayList(COMMENT_MOMENTS, LIKE_MOMENTS);
        List<MemberMsg> memberMsgs = memberMsgRepository.findByReceiverTokenAndIsDeleteAndTypeIn(userToken, NO, memberMsgTypes);
        List<MemberMsg> list = new ArrayList<>();
        for (MemberMsg memberMsg : memberMsgs) {
            if (memberMsg.getId() <= startMessageId) {
                memberMsg.setIsDelete(YES);
                list.add(memberMsg);
            }
        }
        memberMsgRepository.save(list);
    }

    public void delMessage(String userToken, Long id) {
        MemberMsg memberMsg = memberMsgRepository.findOne(id);
        ifFalseThrow(memberMsg.getReceiver().getToken().equals(userToken), TIP_NO_AUTHORITY);
        memberMsg.setIsDelete(YES);
        memberMsgRepository.save(memberMsg);
    }

    public String readingMsg(String userToken, MemberMsgType type) {
        Member member = memberRepository.findByToken(userToken);
        ifNullThrow(member, TIP_NO_MEMBER);
        List<MemberMsg> list = memberMsgRepository.findByReceiverTokenAndTypeAndIsDeleteAndIsRead(userToken, type, NO, NO);
        ifTrueThrow(list.isEmpty(), TIP_NO_UNREAD_MSG);
        list.forEach(t -> {
            if (t.getIsRead() == NO) {
                t.setIsRead(YES);
                memberMsgRepository.save(t);
            }
        });
        return "success";
    }


}