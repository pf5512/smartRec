package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.School;

/**
 * Created by mu.jie on 2016/11/24.
 */
public interface SchoolRepository extends BaseRepository<School> {
    School findByMemberId(Long id);

    School findByMemberToken(String userToken);
}
