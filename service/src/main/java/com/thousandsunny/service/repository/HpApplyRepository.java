package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.HpApply;

/**
 * Created by admin on 2016/10/25.
 */
public interface HpApplyRepository extends BaseRepository<HpApply>{
    HpApply findByIdAndState(Long id, ModuleKey.ApplyEnum state);
}
