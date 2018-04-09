package com.thousandsunny.cms.domain.repository;

import com.thousandsunny.cms.model.Role;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;

import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在7/22/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface RoleRepository extends BaseRepository<Role> {
    List<Role> findByIsDelete(ModuleKey.BooleanEnum yes);
}
