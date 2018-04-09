package com.thousandsunny.core.domain.repository;

import com.thousandsunny.core.model.Region;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * 如果这些代码有用，那它们是guitarist在9/21/16写的;
 * 如果没用，那我就不知道是谁写的了。
 */
public interface RegionRepository extends BaseRepository<Region> {
    List<Region> findByParentIsNull();

    List<Region> findByParentId(Long provinceId);

    Region findByIdAndRegionLevel(Long id, Integer level);

    List<Region> findByIdNotIn(List<Long> ids);

    List<Region> findByRegionLevelAndIsHotCity(Integer level, BooleanEnum isHotCity);

    List<Region> findByRegionLevelAndNameContaining(Integer level, String name);

    Region findByRegionLevelAndParentIdAndNameContaining(Integer level, Long parentId, String name);

    List<Region> findByRegionLevel(Integer level);
}