package com.thousandsunny.thirdparty.domain.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.MemberGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by admin on 2016/10/12.
 */
public interface MemberGroupRepository extends BaseRepository<MemberGroup> {

    Page<MemberGroup> findByGroupIdAndIsDeleteOrderByIsOwnerDesc(Long gId, ModuleKey.BooleanEnum isDelete, Pageable pageable);

    List<MemberGroup> findByGroupIdAndIsDeleteOrderByIsOwnerDesc(Long gId, ModuleKey.BooleanEnum isDelete);

    Page<MemberGroup> findByGroupIdAndMemberUsernameContainingAndIsDeleteOrderByIsOwnerDesc(Long gId, String username, ModuleKey.BooleanEnum isDelete, Pageable pageable);

    Page<MemberGroup> findByGroupIdAndMemberRealNameContainingAndIsDeleteOrderByIsOwnerDesc(Long gId, String username, ModuleKey.BooleanEnum isDelete, Pageable pageable);

    Integer countByGroupId(Long gId);

    MemberGroup findByMemberTokenAndGroupIdAndIsDelete(String token, Long gId, ModuleKey.BooleanEnum isDelete);

    MemberGroup findByMemberTokenAndGroupHxGroupIdAndIsDelete(String token, String hxGroupId, ModuleKey.BooleanEnum isDelete);

    List<MemberGroup> findByMemberTokenAndIsDelete(String token, ModuleKey.BooleanEnum isDelete);

    List<MemberGroup> findByMemberUsernameAndGroupIdAndIsDelete(String username, Long gId, ModuleKey.BooleanEnum isDelete);

    List<MemberGroup> findByGroupIdAndIsDelete(Long gId, ModuleKey.BooleanEnum isDelete);

}
