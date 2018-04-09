package com.thousandsunny.core.domain.repository;


import com.thousandsunny.core.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserRepository extends BaseRepository<User> {

    Page<User> findByOrgId(Long id, Pageable pageRequest);

    List<User> findByOrgId(Long id);

    User findByMobile(String mobile);

    User findByUsername(String username);
}
