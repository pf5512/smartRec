package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.service.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by admin on 2016/10/24.
 */
public interface ShopRepository extends BaseRepository<Shop> {
    Shop findByOwnerToken(String userToken);

    Shop findByOwnerId(Long id);

    Integer countByAreaId(Long id);

    Page<Shop> findByProvinceInAndCityInAndAreaIn(List<Region> province, List<Region> city, List<Region> area, Pageable pageable);

    Page<Shop> findByAreaIdAndStateOrderByDateDesc(Long areaId, BooleanEnum state, Pageable pageable);

    Shop findByIdAndIsDelete(Long id, BooleanEnum no);
}


