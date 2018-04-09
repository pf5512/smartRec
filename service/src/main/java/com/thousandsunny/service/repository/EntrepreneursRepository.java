package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.Entrepreneurs;

/**
 * Created by admin on 2016/11/1.
 */
public interface EntrepreneursRepository extends BaseRepository<Entrepreneurs> {
    Entrepreneurs findByMemberToken(String token);

    Long countByMemberId(Long id);
}
