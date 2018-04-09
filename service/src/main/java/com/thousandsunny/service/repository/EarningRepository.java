package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.Earning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.thousandsunny.service.ModuleKey.EarningType;
/**
 * Created by admin on 2016/11/2.
 */
public interface EarningRepository extends BaseRepository<Earning> {
    Page<Earning> findByEntrepreneursMemberToken(String token, Pageable pageable);
    Page<Earning> findByEntrepreneursMemberTokenAndEarningType(String token, ModuleKey.EarningType earningType, Pageable pageable);
    Page<Earning> findByPartnerMemberTokenAndEarningTypeOrEarningType(String userToken,EarningType earningType1, EarningType earningType2, Pageable pageable);

}
