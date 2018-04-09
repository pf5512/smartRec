package com.thousandsunny.core.domain.service;

import com.thousandsunny.core.ModuleKey.ApplyState;
import com.thousandsunny.core.domain.repository.FriendApplyRepository;
import com.thousandsunny.core.domain.repository.FriendsRepository;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.model.FriendApply;
import com.thousandsunny.core.model.Friends;
import com.thousandsunny.core.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.ApplyState.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.*;

/**
 * Created by Administrator on 2016/9/21 0021.
 */
@Service
public class BaseFriendsService extends BaseService<Friends> {
    @Autowired
    private FriendsRepository friendsRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FriendApplyRepository friendApplyRepository;
    private List<ApplyState> applyStates = newArrayList(AGREE, APPLY);


    /**
     * 添加好友
     */
    public FriendApply addFriend(String userToken, String invitedUserToken) {
        ifTrueThrow(userToken.equals(invitedUserToken),TIP_INVALID_DATA);
        Member user = memberRepository.findByToken(userToken);
        ifNullThrow(user, TIP_NO_MEMBER);
        Member invitedUser = memberRepository.findByToken(invitedUserToken);
        ifNullThrow(invitedUser, TIP_NO_MEMBER);
        FriendApply friendApply = friendApplyRepository.findByApplicantTokenAndApproverToken(userToken, invitedUserToken);
        ifFalseThrow(friendApply == null, TIP_HAS_SUBMIT);
        friendApply = new FriendApply();
        friendApply.setApplicant(user);
        friendApply.setApprover(invitedUser);
        friendApply.setApplyDate(new Date());
        friendApply.setApplyState(APPLY);
        friendApply.setIsRead(NO);
        friendApplyRepository.save(friendApply);
        return friendApply;
    }

    /**
     * 接受好友请求
     *
     * @Date 2016/9/22 0022
     * @Author LouChongXiao
     */
    public void acceptApply(String userToken, FriendApply friendApply) {
        Member member = memberRepository.findByToken(userToken);
        ifNullThrow(friendApply, TIP_NO_SUBMIT);
        friendApply.setApplyState(AGREE);
        friendApply.setIsRead(YES);
        friendApply = friendApplyRepository.save(friendApply);
        Friends friends = new Friends();
        Friends friends1 = new Friends();
        friends.setFriend(friendApply.getApplicant());
        friends.setOwner(member);
        friends1.setFriend(member);
        friends1.setOwner(friendApply.getApplicant());
        friends.setKnowDate(new Date());
        friends1.setKnowDate(new Date());
        friendsRepository.save(friends);
        friendsRepository.save(friends1);
    }

    /**
     * 删除好友请求
     *
     * @Date 2016/9/22 0022
     * @Author LouChongXiao
     */
    public void refuseApply(String userToken, Long applyId) {
        FriendApply friendApply = friendApplyRepository.findOne(applyId);
        if (userToken.equals(friendApply.getApprover().getToken())) {
            friendApply.setApplyState(REJECT);
            friendApplyRepository.save(friendApply);
        } else if (userToken.equals(friendApply.getApplicant().getToken())) {
            friendApplyRepository.delete(applyId);
        }
    }

    public Boolean isFriends(String userToken, String checkedUserToken) {
        return friendsRepository.findByOwnerTokenAndFriendToken(userToken, checkedUserToken) != null;
    }

    /**
     * 好友申请列表
     *
     * @Date 2016/9/23 0023
     * @Author LouChongXiao
     */
    public Page<FriendApply> applyList(String userToken, Pageable pageable) {
        Page<FriendApply> friendApplies = friendApplyRepository.findByApproverTokenAndApplyStateInOrderByApplyDateDesc(userToken, applyStates, pageable);
        return friendApplies;
    }


}
