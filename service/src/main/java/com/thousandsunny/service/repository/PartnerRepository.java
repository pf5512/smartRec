package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.model.Partner;

import java.util.List;

public interface PartnerRepository extends BaseRepository<Partner> {
    List<Partner> findByMemberTokenOrderByDate(String userToken);

    Integer countByMemberToken(String token);

    Partner findByArea(Region area);

    Long countByMemberId(Long memberId);

    Partner findByMemberIdAndAreaId(Long userId, Long areaId);
}
