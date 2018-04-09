package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.PartnerApply;

import java.util.List;

import static com.thousandsunny.service.ModuleKey.ApplyEnum;

public interface PartnerApplyRepository extends BaseRepository<PartnerApply> {
    List<PartnerApply> findByMemberTokenOrderByDate(String token);

    PartnerApply findByMemberTokenAndState(String userToken, ModuleKey.ApplyEnum state);

    PartnerApply findByProvinceIdAndCityIdAndAreaId(Long id1, Long id2, Long id3);

    List<PartnerApply> findByStateIn(List<ApplyEnum> enums);

    PartnerApply findByProvinceIdAndCityIdAndAreaIdAndStateIn(Long id1, Long id2, Long id3, List<ApplyEnum> list);

    List<PartnerApply> findByMemberTokenAndStateIn(String token, List<ApplyEnum> applyAreaList);
}
