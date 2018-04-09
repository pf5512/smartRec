package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.ResumeBlocked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface ResumeBlockedRepository extends BaseRepository<ResumeBlocked> {

    List<ResumeBlocked> findByMemberToken(String userToken);

    List<ResumeBlocked> findByResumeMemberToken(String userToken);

    ResumeBlocked findByMemberTokenAndResumeMemberToken(String userToken, String resumeMemberToken);

    ResumeBlocked findByMemberTokenAndResumeMemberMobile(String userToken, String resumeMemberMobile);

    ResumeBlocked findByResumeMemberMobile(String mobile);

    Page<ResumeBlocked> findByMemberToken(String userToken, Pageable pageRequest);
}
