package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.TerraceActivityClass;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * Created by Xiaoxuewei on 2016/11/30.
 */
public interface TerraceActivityClassRepository extends BaseRepository<TerraceActivityClass> {
    /**
     * 分页查询平台活动类别列表
     *
     * @param pageable
     * @return
     */
    List<TerraceActivityClass> findByIsDelete(BooleanEnum isDelete);
}
