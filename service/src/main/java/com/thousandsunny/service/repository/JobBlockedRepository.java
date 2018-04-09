package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.JobBlocked;

/**
 * Created by admin on 2016/10/21.
 */
public interface JobBlockedRepository extends BaseRepository<JobBlocked>{
    JobBlocked findByMemberTokenAndJobId(String token,Long id);

}
