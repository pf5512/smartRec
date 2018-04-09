package com.thousandsunny.thirdparty.domain.repository;


import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.thirdparty.model.TpAccountApplyRecord;

public interface TpAccountApplyRecordRepository extends BaseRepository<TpAccountApplyRecord> {

    TpAccountApplyRecord findByOrderNo(String orderNo);
}
