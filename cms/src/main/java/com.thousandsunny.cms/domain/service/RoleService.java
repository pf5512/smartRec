package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.domain.repository.MemberRoleRepository;
import com.thousandsunny.cms.domain.repository.RoleRepository;
import com.thousandsunny.cms.model.MemberRole;
import com.thousandsunny.cms.model.Role;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;

/**
 * 如果这些代码有用，那它们是guitarist在8/23/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Service
public class RoleService extends BaseService<Role> {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MemberRoleRepository memberRoleRepository;

    public List<Role> findRoleList() {
        return roleRepository.findByIsDelete(NO);
    }

    public List<MemberRole> findMemberRoles(Member member) {
        return memberRoleRepository.findByMemberAndIsDelete(member, NO);
    }
}
