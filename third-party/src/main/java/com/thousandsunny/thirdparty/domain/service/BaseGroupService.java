package com.thousandsunny.thirdparty.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Group;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberGroup;
import com.thousandsunny.thirdparty.domain.repository.GroupRepository;
import com.thousandsunny.thirdparty.domain.repository.MemberGroupRepository;
import com.thousandsunny.thirdparty.easemob.comm.body.ChatGroupBody;
import com.thousandsunny.thirdparty.easemob.comm.wrapper.ResponseWrapper;
import com.thousandsunny.thirdparty.easemob.service.EasemobService;
import com.thousandsunny.thirdparty.vo.OperationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.TalkType.NORMAL_GROUP_TALK;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;
import static java.util.Objects.isNull;
import static org.hibernate.internal.util.collections.CollectionHelper.isNotEmpty;

/**
 * Created by admin on 2016/10/12.
 */
@Service
public class BaseGroupService extends BaseService<Group> {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MemberGroupRepository memberGroupRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EasemobService easemobService;

    /**
     * 获取群聊信息
     */
    public MemberGroup findOneGroup(String userToken, String hxGroupId,Long groupId) {
        MemberGroup memberGroup = null;
        if(groupId!=null){
            memberGroup = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(userToken,groupId,NO);
        }else {
            memberGroup = memberGroupRepository.findByMemberTokenAndGroupHxGroupIdAndIsDelete(userToken, hxGroupId, NO);
        }
        ifTrueThrow(isNull(memberGroup), TIP_NO_FIND);
        return memberGroup;
    }


    /**
     * 建立群聊
     */
    public Group setUp(OperationPrincipal sysTaskApproval) {
        List<String> tokens = new ArrayList<>();
        ifTrueThen(isNotEmpty(sysTaskApproval.getTokens()), () -> sysTaskApproval.getTokens().forEach(e -> tokens.add(e.getUserToken())));
        List<MemberGroup> memberGroups = new ArrayList<>();
        if (isNotEmpty(tokens)) {
            tokens.forEach(e -> {
                Member member = memberRepository.findByToken(e);
                ifTrueThrow(isNull(member), TIP_NO_CHANSHUERROR);
                MemberGroup mGroup = new MemberGroup();
                mGroup.setMember(member);
                mGroup = memberGroupRepository.save(mGroup);
                memberGroups.add(mGroup);
            });
        }
        Member member = memberRepository.findByToken(sysTaskApproval.getUserToken());
        tokens.add(member.getToken());
        ifTrueThrow(isNull(member), TIP_NO_CHANSHUERROR);
        MemberGroup memberGroup = new MemberGroup();
        memberGroup.setMember(member);
        memberGroup.setIsOwner(YES);
        memberGroup.setIsTop(NO);
        memberGroup = memberGroupRepository.save(memberGroup);
        memberGroups.add(memberGroup);

        String groupName = memberGroups.get(memberGroups.size() - 1).getMember().getRealName();
        for (int i = 0; i < memberGroups.size() - 1; i++) {
            if (memberGroups.get(i).getMember().getRealName() != null) {
                groupName = groupName.concat("、").concat(memberGroups.get(i).getMember().getRealName());
            }
            continue;
        }
        Group group = new Group();
        group.setMemberGroups(memberGroups);
        group.setName(groupName);
        group.setDate(new Date());
        group.setType(NORMAL_GROUP_TALK);
        group.setChairMan(memberGroup);
        group.setHxGroupId(getHXGroupId(tokens, groupName));
        Group group1 = groupRepository.save(group);
        memberGroups.forEach(e -> e.setGroup(group1));
        memberGroupRepository.save(memberGroups);
        return group1;
    }

    private String getHXGroupId(List<String> tokens, String groupName) {
        String strings[] = new String[tokens.size()];
        for (int i = 0, j = tokens.size(); i < j; i++) {
            strings[i] = tokens.get(i);
        }
        ChatGroupBody chatGroupBody = new ChatGroupBody(groupName, "ASC", true, 200l, true, null, strings);
        ResponseWrapper s = easemobService.createChatGroup(chatGroupBody);
        JSONObject jsonObject = parseObject(s.getResponseBody().toString());
        return parseObject(jsonObject.getString("data")).getString("groupid");
    }

    /**
     * 群消息免打扰
     */

    public void noDisturb(String userToken, Long groupId, OperatorType operatorType) {
        MemberGroup memberGroup = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(userToken, groupId, NO);
        ifNullThrow(memberGroup, TIP_NO_CHANSHUERROR);
        memberGroup.setIsNoDisturb(operatorType == SURE ? YES : NO);
        memberGroupRepository.save(memberGroup);

    }


    /**
     * 群消息置顶
     */
    public void toTop(String userToken, Long groupId, OperatorType operatorType) {
        MemberGroup memberGroup = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(userToken, groupId, NO);
        ifNullThrow(memberGroup, TIP_NO_CHANSHUERROR);
        memberGroup.setIsTop(operatorType == SURE ? YES : NO);
        memberGroupRepository.save(memberGroup);
    }


    /**
     * 群成员添加
     */
    public Group addGroupMate(OperationPrincipal sysTaskApproval, Long gId) {
        MemberGroup memberGroup = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(sysTaskApproval.getUserToken(), gId, NO);
        ifTrueThrow(memberGroup == null, TIP_NO_AUTHORITY);
        List<String> tokens = new ArrayList<>();
        if (sysTaskApproval.getTokens() != null) {
            sysTaskApproval.getTokens().forEach(e -> tokens.add(e.getUserToken()));
        }
        Group group = groupRepository.findOne(gId);
        List<MemberGroup> memberGroups = group.getMemberGroups();
        List<MemberGroup> ms = new ArrayList<MemberGroup>();
        ms.addAll(memberGroups);

//        List<Member> members = memberRepository.findByTokenInAndIsDelete(tokens,NO);
//        for(Member member:members){
//            MemberGroup memberGroup1 = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(member.getToken(), gId, NO);
//            if (isNull(memberGroup1)) {
//                MemberGroup mGroup = new MemberGroup();
//                mGroup.setMember(member);
//                mGroup = memberGroupRepository.save(mGroup);
//                memberGroups.add(mGroup);
//                ms.add(mGroup);
//            }
//        }

        tokens.forEach(e -> {
            Member member = memberRepository.findByTokenAndIsDelete(e, NO);
            ifNullThrow(member, TIP_NO_MEMBER);
            MemberGroup memberGroup1 = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(member.getToken(), gId, NO);
            if (isNull(memberGroup1)) {
                MemberGroup mGroup = new MemberGroup();
                mGroup.setMember(member);
                mGroup = memberGroupRepository.save(mGroup);
                memberGroups.add(mGroup);
                ms.add(mGroup);
            }
        });
        group.setMemberGroups(ms);
//        group.setMemberGroups(memberGroups);
        Group group1 = groupRepository.save(group);
        memberGroups.forEach(e -> e.setGroup(group1));
        memberGroupRepository.save(memberGroups);
        addBatchUsersToChatGroup(group, tokens);
        return group;
    }

    private void addBatchUsersToChatGroup(Group group, List<String> tokens) {
        for (int i = 0, j = tokens.size(); i < j; i++) {
            easemobService.addSingleUserToChatGroup(group.getHxGroupId(), tokens.get(i));
        }
    }

    /**
     * 群成员删除
     */
    public void delGroupMate(OperationPrincipal sysTaskApproval, Long gId) {
        MemberGroup memberGroup = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(sysTaskApproval.getUserToken(), gId, NO);
        ifNullThrow(memberGroup, TIP_NO_AUTHORITY);
        ifTrueThrow(memberGroup.getIsOwner() == NO, TIP_NO_AUTHORITY);
        List<String> tokens = new ArrayList<>();
        if (sysTaskApproval.getTokens() != null) {
            sysTaskApproval.getTokens().forEach(e -> tokens.add(e.getUserToken()));
        }
        Group group = groupRepository.findOne(gId);
        List<MemberGroup> memberGroups = group.getMemberGroups();

        memberGroups = simpleFilter(memberGroups, x -> !tokens.contains(x.getMember().getToken()));

        if (tokens.contains(sysTaskApproval.getUserToken())) {
            wrapMemberGroup(group, memberGroups);
        }

        String username = " ";
        String oldgroupName = group.getName();
        if (tokens.size() != 0) {
            for (int i = 0; i < tokens.size(); i++)
                username = memberRepository.findByTokenAndIsDelete(tokens.get(i), NO).getRealName();
                username = username==null?"":username;
            if (oldgroupName != null) {
                if (oldgroupName.contains("、" + username)) {
                    oldgroupName = oldgroupName.replace("、" + username, "");
                } else if (oldgroupName.contains(username + "、")) {
                    oldgroupName = oldgroupName.replace(username + "、", "");
                } else {
                    oldgroupName = oldgroupName.replace(username, ""); //如果只有一个成员的情况
                }
            }
        }
        group.setName(oldgroupName);

        group.setMemberGroups(memberGroups);
        groupRepository.save(group);
        removeBatchUsersFromChatGroup(group.getHxGroupId(), tokens);
        tokens.forEach(e -> {
            MemberGroup tokenAndGroupIdAndIsDelete = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(e, gId, NO);
            memberGroupRepository.delete(tokenAndGroupIdAndIsDelete);
        });
    }

    private void wrapMemberGroup(Group group, List<MemberGroup> memberGroups) {
        MemberGroup memberGroup1 = memberGroups.get(0);
        memberGroup1.setIsOwner(YES);
        memberGroup1.setIsTop(YES);
        memberGroup1 = memberGroupRepository.save(memberGroup1);
        group.setChairMan(memberGroup1);
    }

    public void removeBatchUsersFromChatGroup(String groupId, List<String> tokens) {
        String strings[] = new String[tokens.size()];
        for (int i = 0, j = tokens.size(); i < j; i++) {
            strings[i] = tokens.get(i);
        }
        easemobService.removeBatchUsersFromChatGroup(groupId, strings);
    }


    /**
     * 退出群聊
     */
    public void outGroup(String userToken, Long groupId) {
        MemberGroup memberGroup = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(userToken, groupId, NO);
        ifTrueThrow(memberGroup == null, TIP_JOIN_A_GROUP_BEFORE);
        Group group = groupRepository.findOne(groupId);
        List<MemberGroup> memberGroups = group.getMemberGroups();
        if (memberGroups.size() == 1) {
            group.setChairMan(null);
            groupRepository.save(group);
            memberGroupRepository.delete(memberGroup);
            group.setIsDelete(YES);
            groupRepository.save(group);
            ifTrueThrow(memberGroups.size() == 1, TIP_JOIN_A_GROUP_NOUSER);
        }
        //   ifTrueThrow(memberGroups.size() == 0, TIP_JOIN_A_GROUP_NOUSER);
        else {
           memberGroups =  memberGroups.stream().filter(e -> userToken.equals(e.getMember().getToken())).collect(Collectors.toList());
            if (memberGroup.getIsOwner() == YES ) {
                wrapMemberGroup(group, memberGroups);
            }

            group.setMemberGroups(memberGroups);
            groupRepository.save(group);
            memberGroup.setIsDelete(YES);
            memberGroupRepository.save(memberGroup);
            memberGroupRepository.delete(memberGroup);
        }
        easemobService.removeSingleUserFromChatGroup(group.getHxGroupId(), userToken);
    }


    /**
     * 加入群聊
     */
    public MemberGroup inGroup(String userToken, Long groupId) {
        MemberGroup memberGroup1 = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(userToken, groupId, NO);
        ifTrueThrow(memberGroup1 != null, TIP_JOIN_C_GROUP_BEFORE);

        MemberGroup memberGroup = new MemberGroup();
        Member member = memberRepository.findByTokenAndIsDelete(userToken, NO);

        ifTrueThrow(member == null, TIP_NO_CHANSHUERROR);
        memberGroup.setMember(member);

        Group group = groupRepository.findOne(groupId);
        ifTrueThrow(group == null, TIP_NO_CHANSHUERROR);

        List<MemberGroup> memberGroups = group.getMemberGroups();
        memberGroups.add(memberGroup);

        group.setMemberGroups(memberGroups);
        groupRepository.save(group);
        memberGroup.setIsDelete(NO);
        memberGroup.setIsOwner(NO);
        memberGroup.setGroup(group);

        easemobService.addSingleUserToChatGroup(group.getHxGroupId(), userToken);
        return memberGroupRepository.save(memberGroup);
    }


    /**
     * 群成员列表
     */
    public Page<MemberGroup> groupMateList(Long groupId, Integer pageNo, Integer pageSize) {
        Group group = groupRepository.findOne(groupId);
        ifFalseThrow(group != null, TIP_JOIN_NO_GROUP);
        if (pageSize == null) {
            Integer count = memberGroupRepository.countByGroupId(groupId);
            PageVO pageVO = new PageVO();
            pageVO.setPageSize(count);
            return memberGroupRepository.findByGroupIdAndIsDeleteOrderByIsOwnerDesc(groupId, NO, pageVO.pageRequest());
        } else {
            PageVO pageVO = new PageVO();
            pageVO.setPageNo(pageNo);
            pageVO.setPageSize(pageSize);
            return memberGroupRepository.findByGroupIdAndIsDeleteOrderByIsOwnerDesc(groupId, NO, pageVO.pageRequest());
        }
    }

    public List<MemberGroup> groupMateList(Long groupId,String hxGroupId) {
        Group group = null;
        if(hxGroupId!=null&&"".equals(hxGroupId)){
            group = groupRepository.findByHxGroupIdAndIsDelete(hxGroupId,NO);
        }
        group = groupRepository.findOne(groupId);
        ifFalseThrow(group != null, TIP_JOIN_NO_GROUP);
        return memberGroupRepository.findByGroupIdAndIsDeleteOrderByIsOwnerDesc(groupId,NO);
    }


    /**
     * 群主转让
     */
    public void changeOwner(String ownerUserToken, String userToken, Long groupId) {
        MemberGroup memberGroup = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(userToken, groupId, NO);
        ifTrueThrow(memberGroup == null, TIP_JOIN_B_GROUP_BEFORE);

        MemberGroup memberGroupOwner = memberGroupRepository.findByMemberTokenAndGroupIdAndIsDelete(ownerUserToken, groupId, NO);
        ifTrueThrow(memberGroupOwner == null, TIP_JOIN_A_GROUP_BEFORE);
        ifTrueThrow(memberGroupOwner.getIsOwner() == NO, TIP_NO_AUTHORITY);
        Group group = groupRepository.findOne(groupId);
        if (memberGroup != null && group != null) {
            memberGroup.setIsOwner(YES);
            memberGroupRepository.save(memberGroup);

            memberGroupOwner.setIsOwner(NO);
            memberGroupRepository.save(memberGroupOwner);

            group.setChairMan(memberGroup);
            groupRepository.save(group);
        }

    }


    /**
     * 群成员搜索
     */
    public Page<MemberGroup> search(Long groupId, String keyword, Integer pageNo, Integer pageSize) {
        Group group = groupRepository.findOne(groupId);
        ifFalseThrow(group != null, TIP_JOIN_NO_GROUP);
        if (pageSize == null) {
            Integer count = memberGroupRepository.countByGroupId(groupId);
            PageVO pageVO = new PageVO();
            pageVO.setPageSize(count);
            return memberGroupRepository.findByGroupIdAndMemberRealNameContainingAndIsDeleteOrderByIsOwnerDesc(groupId, keyword, NO, pageVO.pageRequest());
        } else {
            PageVO pageVO = new PageVO();
            pageVO.setPageNo(pageNo);
            pageVO.setPageSize(pageSize);
            return memberGroupRepository.findByGroupIdAndMemberRealNameContainingAndIsDeleteOrderByIsOwnerDesc(groupId, keyword, NO, pageVO.pageRequest());
        }
    }

    /**
     * 2.15修改群名称
     */
    public MemberGroup updateGroupName(String userToken,String name,Long groupId,String hxGroupId){
        MemberGroup memberGroup = findOneGroup(userToken, hxGroupId,groupId);
        Group group = memberGroup.getGroup();
        ifTrueThrow(group == null, TIP_JOIN_NO_GROUP);
        group.setName(name);
        return memberGroup;
    }


}
