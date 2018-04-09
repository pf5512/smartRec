package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.MomentsBlocked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/10/25.
 */
public interface MomentsBlockedRepository extends BaseRepository<MomentsBlocked> {

    MomentsBlocked findByMemberTokenAndMomentsMemberToken(String userToken, String momentsMemberToken);

    Page<MomentsBlocked> findByMemberToken(String userToken, Pageable pageRequest);
}
