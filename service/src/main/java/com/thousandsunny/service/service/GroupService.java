package com.thousandsunny.service.service;

import com.thousandsunny.core.ModuleKey.MemberMsgType;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Group;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberGroup;
import com.thousandsunny.service.model.MemberMsg;
import com.thousandsunny.thirdparty.domain.service.BaseGroupService;
import com.thousandsunny.thirdparty.vo.OperationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.MemberMsgType.GROUP_MEMBER_ADD;
import static com.thousandsunny.core.ModuleKey.MemberMsgType.GROUP_TALK;

@Service
public class GroupService extends BaseService<Group> {
    @Autowired
    private MemberMsgService memberMsgService;
    @Autowired
    private BaseGroupService baseGroupService;

    /**
     * 建立群聊
     */
    public Group setUp(OperationPrincipal sysTaskApproval) {
        Group group = baseGroupService.setUp(sysTaskApproval);
        sendMemberMsg(group,GROUP_TALK);
        return group;
    }

    /**
     * 群成员添加
     */
    public Group addGroupMate(OperationPrincipal sysTaskApproval, Long gId) {
        Group group = baseGroupService.addGroupMate(sysTaskApproval, gId);
        sendMemberMsg(group,GROUP_MEMBER_ADD);
        return group;
    }

    private void sendMemberMsg(Group group,MemberMsgType type) {
        List<MemberGroup> memberGroups = group.getMemberGroups();
        memberGroups.forEach(e -> {
            memberMsgService.updateMemberMsgIsNew(e.getMember(),type);
            MemberMsg memberMsg = new MemberMsg();
            memberMsg.setGroupTalk(group);
            memberMsg.setReceiver(e.getMember());
            memberMsg.setTitle(group.getName());
            memberMsg.setContent(group.getName());
            memberMsg.setType(type);
            memberMsgService.save(memberMsg);
        });
    }
}
