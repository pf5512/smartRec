package com.thousandsunny.thirdparty.domain.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.thirdparty.model.AccountFreezingRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by mu.jie on 2017/2/27.
 */
public interface AccountFreezingRecordRepository extends BaseRepository<AccountFreezingRecord> {
        List<AccountFreezingRecord> findByIsUnfreezeAndUnfreezeDateBetween(BooleanEnum no, Date date, Date now);
}
