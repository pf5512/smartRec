package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.TransferRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/10/12.
 */
public interface TransferRecordRepository extends BaseRepository<TransferRecord> {
    TransferRecord findByAssignorAndValidAndIsDelete(Member assignor, Boolean aTrue, BooleanEnum no);

    TransferRecord findByIdAndIsDelete(Long id, BooleanEnum no);

    Page<TransferRecord> findByShopIdAndIsDelete(Long shopId,BooleanEnum isDelete, Pageable pageable);
}
