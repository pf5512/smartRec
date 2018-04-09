package com.thousandsunny.cms.domain.repository;

import com.thousandsunny.cms.model.MemberRole;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Member;

import java.util.List;

/**
 * Created by 13336 on 2017/3/10.
 */
public interface MemberRoleRepository extends BaseRepository<MemberRole> {
    List<MemberRole> findByMemberAndIsDelete(Member member, ModuleKey.BooleanEnum isDelete);
}
