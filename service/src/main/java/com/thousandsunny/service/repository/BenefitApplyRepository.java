package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey.BenefitApplyState;
import com.thousandsunny.service.ModuleKey.BenefitType;
import com.thousandsunny.service.model.BenefitApply;

import java.util.List;

/**
 * Created by mu.jie on 2016/11/21.
 */
public interface BenefitApplyRepository extends BaseRepository<BenefitApply> {

    BenefitApply findByMemberIdAndTypeAndIsDelete(Long memberId, BenefitType type, BooleanEnum no);

    List<BenefitApply> findByApplyStateAndTypeIn(BenefitApplyState review, List<BenefitType> benefitTypes);
}
