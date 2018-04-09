package com.thousandsunny.service.repository;

import com.thousandsunny.cms.model.Commentary;
import com.thousandsunny.core.ModuleKey.*;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.FriendApply;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.MemberMsg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在9/23/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface MemberMsgRepository extends BaseRepository<MemberMsg> {
    List<MemberMsg> findByReceiverTokenAndIsNewAndIsDeleteAndTypeInOrderByDateDesc(String token,BooleanEnum flag, BooleanEnum isDelete, List<MemberMsgType> memberMsgTypes);
    Page<MemberMsg> findByReceiverTokenAndIsDeleteAndTypeOrderByDateDesc(String token, BooleanEnum isDelete, MemberMsgType type, Pageable pageable);

    List<MemberMsg> findByReceiverAndIsDeleteAndTypeOrderByDateDesc(Member receiver, BooleanEnum isDelete, MemberMsgType type);

    List<MemberMsg> findByReceiverTokenAndIsDeleteAndTypeIn(String token, BooleanEnum isDelete, List<MemberMsgType> types);

    Long countByReceiverAndIsDeleteAndIsReadAndType(Member receiver, BooleanEnum flag, BooleanEnum isRead,MemberMsgType types);

    List<MemberMsg> findByReceiverAndIsDeleteAndIsNewAndType(Member member, BooleanEnum no,BooleanEnum yes,MemberMsgType type);

    List<MemberMsg> findByReceiverAndIsDeleteAndTypeInAndIsReadOrderByDateDesc(Member member, BooleanEnum no,List<MemberMsgType> types,BooleanEnum flag);

    Page<MemberMsg> findByReceiverAndIsDeleteAndTypeInAndIsReadOrderByDateDesc(Member member, BooleanEnum no,List<MemberMsgType> types,BooleanEnum flag, Pageable pageable);

    List<MemberMsg> findByReceiverTokenAndTypeAndIsDeleteAndIsRead(String userToken, MemberMsgType type, BooleanEnum isDelete, BooleanEnum isRead);

    MemberMsg findByReceiverTokenAndIsDeleteAndFriendApply(String userToken, BooleanEnum no, FriendApply friendApply);

    List<MemberMsg> findByCommentary(Commentary commentary);
}
