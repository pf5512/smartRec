package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.model.MemberVisit;

/**
 * Created by mu.jie on 2016/12/22.
 */
public interface MemberVisitRepository extends BaseRepository<MemberVisit> {
    MemberVisit findTop1ByMemberIdOrderByCreateTimeDesc(Long id);

    MemberVisit findTop1ByDeviceVersionOrderByCreateTimeDesc(String deviceVersion);
}
