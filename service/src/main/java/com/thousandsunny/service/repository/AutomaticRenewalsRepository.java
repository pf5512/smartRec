package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.AutomaticRenewals;

import java.sql.Date;
import java.util.List;

public interface AutomaticRenewalsRepository extends BaseRepository<AutomaticRenewals> {
    AutomaticRenewals findByJobIdAndWorkerTokenAndIsDelete(Long id, String userToken, BooleanEnum isDelete);

    AutomaticRenewals findByJobIdAndWorkerIdAndIsDelete(Long jobId, Long workerId, BooleanEnum isDelete);

    List<AutomaticRenewals> findByNextTimeAndAuto(Date now, Boolean auto);

    List<AutomaticRenewals> findByIsDelete(BooleanEnum isDelete);

    List<AutomaticRenewals> findByIsDeleteAndFinalDateLessThan(BooleanEnum no, Date date1);

    List<AutomaticRenewals> findByJobIdAndIsDelete(Long jobId, BooleanEnum yes);
}
