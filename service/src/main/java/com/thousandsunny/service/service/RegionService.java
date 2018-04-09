package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.repository.RegionRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.thousandsunny.common.lambda.LambdaUtil.ifTrueThrow;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;
import static com.thousandsunny.service.ModuleTips.*;

/**
 * Created by ekoo on 2016/11/21.
 */
@Service
public class RegionService extends BaseService<Region> {

    @Autowired
    private RegionRepository regionRepository;

    public List<Region> region(Long provinceId, Long cityId) {
        if (provinceId == null && cityId == null) {
            return regionRepository.findByParentIsNull();
        } else if (provinceId != null && cityId == null) {
            return regionRepository.findByParentId(provinceId);
        } else if (provinceId != null && cityId != null) {
            return regionRepository.findByParentId(cityId);
        }
        return null;
    }

    public List<Region> findhotCity() {
        return regionRepository.findByRegionLevelAndIsHotCity(2, YES);
    }

    public Region findPlace(Integer level, String name){
        List<Region> list = regionRepository.findByRegionLevelAndNameContaining(level, name);
        ifTrueThrow(list.isEmpty(), TIP_NO_PROVINCE);
        ifTrueThrow((!list.isEmpty()) && list.size() > 1, TIP_MORE_THAN_ONE_PROVINCE);
        return list.get(0);
    }

    public Region findRegion(Integer level, String name, Long parentId) {
        return regionRepository.findByRegionLevelAndParentIdAndNameContaining(level, parentId, name);
    }

    public List<Region> findAllCity() {
        return regionRepository.findByRegionLevel(2);
    }

    public List<Region> findSomeCityList(String keyword) {
        List<Region> list;
        if (StringUtils.isEmpty(keyword))
            list = regionRepository.findByRegionLevel(2);
        else
            list = regionRepository.findByRegionLevelAndNameContaining(2, keyword);
        return list;
    }
}
