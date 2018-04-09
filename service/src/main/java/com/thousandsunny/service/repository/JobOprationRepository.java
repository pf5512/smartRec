package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.JobOpration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/11/29.
 */
public interface JobOprationRepository extends BaseRepository<JobOpration>{

    Page<JobOpration> findByAccountFlowJobId(Long id, Pageable pageable);
}
