package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.RenewalsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface RenewalsRecordRepository extends BaseRepository<RenewalsRecord> {
    Page<RenewalsRecord> findByAccountFlowAccountMemberToken(String userToken, Pageable pageable);

    RenewalsRecord findTop1ByRenewalsJobIdAndRenewalsWorkerIdOrderByDateDesc(Long jobId, Long workerId);

    @Query("from RenewalsRecord r where r.date<:d and r.assigned=:a")
    List<RenewalsRecord> findByDateLessThanAndAssigned(@Param("d") Date oneMonthBefore, @Param("a") Boolean assigned);

    Page<RenewalsRecord> findByAccountFlowAccountMemberTokenAndAccountFlowJobId(String userToken, Long jobId, Pageable pageable);

    RenewalsRecord findByRenewalsIdAndTimes(Long renewalsId, Integer times);

    RenewalsRecord findByRenewalsIdAndTimesAndRenewType(Long id, Integer times, ModuleKey.RenewType failed);
}
