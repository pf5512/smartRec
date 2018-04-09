package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.MemberChatProfile;
import com.thousandsunny.service.repository.MemberChatProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.TIP_NO_CHANSHUERROR;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static java.util.Objects.isNull;

/**
 * Created by admin on 2016/10/12.
 */
@Service
public class MemberChatProfileService extends BaseService<MemberChatProfile> {


    @Autowired
    private MemberChatProfileRepository memberChatProfileRepository;
    @Autowired
    private MemberRepository memberRepository;

    //获取单聊信息
    public MemberChatProfile findOneChatInfo(String userToken, String chatUserToken) {
        MemberChatProfile memberChatProfile = memberChatProfileRepository.findByOwnerTokenAndChatUserToken(userToken, chatUserToken);
        // ifTrueThrow(isNull(memberChatProfile), TIP_NO_FINDSINGLE);
        if (isNull(memberChatProfile)) {
            Member owner = memberRepository.findByTokenAndIsDelete(userToken, NO);
            Member chatUser = memberRepository.findByTokenAndIsDelete(chatUserToken, NO);
            memberChatProfile = new MemberChatProfile();
            memberChatProfile.setIsTop(NO);
            memberChatProfile.setIsNoDisturb(NO);
            memberChatProfile.setLastChat(new Date());
            memberChatProfile.setChatUser(chatUser);
            memberChatProfile.setOwner(owner);
            memberChatProfile = memberChatProfileRepository.save(memberChatProfile);
        }
        return memberChatProfile;
    }

    //单聊免打扰
    public void noDisturb(String userToken, String chatUserToken, OperatorType operatorType) {
        MemberChatProfile memberChatProfile = memberChatProfileRepository.findByOwnerTokenAndChatUserToken(userToken, chatUserToken);
        ifTrueThrow(isNull(memberChatProfile), TIP_NO_CHANSHUERROR);
        memberChatProfile.setIsNoDisturb(operatorType == SURE ? YES : NO);
        memberChatProfileRepository.save(memberChatProfile);

    }


    //单聊置顶
    public void setTop(String userToken, String chatUserToken, OperatorType operatorType) {
        MemberChatProfile memberChatProfile = memberChatProfileRepository.findByOwnerTokenAndChatUserToken(userToken, chatUserToken);
        ifTrueThrow(isNull(memberChatProfile), TIP_NO_CHANSHUERROR);
        memberChatProfile.setIsTop(operatorType == SURE ? YES : NO);
        memberChatProfileRepository.save(memberChatProfile);

    }


}
