package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey.BenefitItemState;
import com.thousandsunny.service.model.BenefitItem;

import java.util.Date;
import java.util.List;

/**
 * Created by mu.jie on 2016/11/19.
 */
public interface BenefitItemRepository extends BaseRepository<BenefitItem> {
    List<BenefitItem> findByBenefitRelIdAndEffectiveDateLessThanAndInvalidDateGreaterThanAndState(Long id, Date now1, Date now2,BenefitItemState state);
}
