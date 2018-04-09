package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.Advertisement;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.*;
import static com.thousandsunny.service.ModuleKey.AdCategoryEnum;

/**
 * Created by mu.jie on 2016/11/23.
 */
public interface AdvertisementRepository extends BaseRepository<Advertisement> {
    @Query("select max(weight) from Advertisement ad")
    Long findMaxWeight();

    List<Advertisement> findByCategoryAndProvinceIdAndCityIdAndAreaIdAndValidAndIsDelete(AdCategoryEnum category, Long provinceId, Long cityId, Long areaId, BooleanEnum valid, BooleanEnum isDelete);

    Advertisement findByCategoryAndValidAndIsDelete(AdCategoryEnum category, BooleanEnum valid, BooleanEnum isDelete);
}
