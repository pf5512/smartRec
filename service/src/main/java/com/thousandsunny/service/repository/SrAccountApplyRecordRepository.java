package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.SrAccountApplyRecord;

/**
 * 如果这些代码有用，那它们是guitarist在21/12/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface SrAccountApplyRecordRepository extends BaseRepository<SrAccountApplyRecord> {
    SrAccountApplyRecord findByOrderNo(String orderNo);
}
