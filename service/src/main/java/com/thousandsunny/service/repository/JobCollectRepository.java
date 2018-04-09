package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.JobCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobCollectRepository extends BaseRepository<JobCollect> {

    JobCollect findByMemberTokenAndJobIdAndCollectEver(String userToken, Long id, BooleanEnum no);

    Page<JobCollect> findByMemberTokenAndCollectEverOrderByDateDesc(String userToken, BooleanEnum no, Pageable pageable);

}
