package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.TerraceActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by Xiaoxuewei on 2016/11/29.
 */
public interface TerraceActivityRepository extends BaseRepository<TerraceActivity> {
    Page<TerraceActivity> findByTaClassId(Long taClassId, Pageable pageable);
}
