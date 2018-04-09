package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.JobType;

import java.util.List;

public interface JobTypeRepository extends BaseRepository<JobType> {
    List<JobType> findByParentJobTypeIsNullAndIsDelete(BooleanEnum no);

    List<JobType> findByParentJobTypeIdAndIsDelete(Long id, BooleanEnum no);

    List<JobType> findByIsDeleteAndParentJobTypeIsNull(BooleanEnum no);
}
