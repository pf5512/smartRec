package com.thousandsunny.cms.domain.repository;

import com.thousandsunny.cms.model.UserRoleRef;
import com.thousandsunny.core.model.User;
import com.thousandsunny.core.domain.repository.BaseRepository;

import java.util.List;

/**
 * 如果这些代码有用，那它们是guitarist在7/26/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface UserRoleRefRepository extends BaseRepository<UserRoleRef> {
    void deleteByUserId(Long id);

    List<UserRoleRef> findByUser(User user);

    List<UserRoleRef> findByRoleId(Long id);

}
