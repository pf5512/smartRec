package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.SchoolApply;

/**
 * Created by mu.jie on 2017/2/14.
 */
public interface SchoolApplyRepository extends BaseRepository<SchoolApply> {
    SchoolApply findByMemberId(Long id);
}
