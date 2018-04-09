package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.ResumeTrainExp;

/**
 * Created by admin on 2016/10/26.
 */
public interface ResumeTrainExpRepository extends BaseRepository<ResumeTrainExp> {
    ResumeTrainExp findByResumeMemberIdAndCourseIdAndIsPlatformAdd(Long id, Long id1, ModuleKey.BooleanEnum yes);
}
