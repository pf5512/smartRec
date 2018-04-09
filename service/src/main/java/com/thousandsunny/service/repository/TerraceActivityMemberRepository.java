package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.TerraceActivityMember;

import java.util.List;

/**
 * Created by Xiaoxuewei on 2016/11/29.
 */
public interface TerraceActivityMemberRepository  extends BaseRepository<TerraceActivityMember> {
    List<TerraceActivityMember> findByMemberTokenAndTerraceActivityId(String userToken, Long id);
}
