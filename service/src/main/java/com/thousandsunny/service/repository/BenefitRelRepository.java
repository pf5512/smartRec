package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey.BenefitType;
import com.thousandsunny.service.model.BenefitRel;

import java.sql.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

public interface BenefitRelRepository extends BaseRepository<BenefitRel> {
    BenefitRel findByMemberTokenAndType(String userToken, BenefitType carFee);

    BenefitRel findByMemberIdAndType(Long userToken, BenefitType carFee);

    BenefitRel findByMemberTokenAndJobIdAndType(String userToken, Long id, BenefitType carFee);

    List<BenefitRel> findByEffectiveDateLessThanAndValidAndTypeIn(Date date, BooleanEnum yes, List<BenefitType> benefitTypes);

    List<BenefitRel> findByMemberTokenAndInvalidDateIsNullOrInvalidDateLessThan(String token, Date date);

    List<BenefitRel> findByMemberTokenAndJobIdAndValid(String userToken, Long jobId, BooleanEnum valid);

    List<BenefitRel> findByMemberTokenAndValid(String receiver,BooleanEnum valid);
}
