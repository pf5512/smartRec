package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey.BenefitType;
import com.thousandsunny.service.model.Benefit;

public interface BenefitRepository extends BaseRepository<Benefit> {
    Benefit findByType(BenefitType type);
}
