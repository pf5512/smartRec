package com.thousandsunny.thirdparty.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.thousandsunny.common.lambda.LambdaUtil;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.AppVersionRepository;
import com.thousandsunny.core.domain.repository.RegionRepository;
import com.thousandsunny.core.model.AppVersion;
import com.thousandsunny.core.model.Region;
import com.thousandsunny.thirdparty.qiniu.SevenCowUploadTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.PhoneType;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by guitarist on 6/28/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Service
public class UtilsService {

    @Autowired
    private AppVersionRepository appVersionRepository;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private SevenCowUploadTokenService tokenService;

    public String uploadToken(String scope, String key) {
        return tokenService.generateUploadToken(scope, key);
//        switch (scope) {
//            case SCOPE_DEFAULT:
//                return tokenService.generateUploadToken(scope, key);
//            default:
//                return tokenService.generateUploadToken(scope, key);
//        }
    }

    public AppVersion checkVersion(PhoneType type) {
        List<AppVersion> versions = appVersionRepository.findByPhoneTypeOrderByUpdateDateDesc(type);
        if (!versions.isEmpty())
            return versions.get(0);
        else return null;
    }

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

    /**
     * 三级省市区
     */
    public List<Region> allRegions() {
        return regionRepository.findAll();
    }

    /**
     * 三级省市区
     * TODO: 暂时开放杭州市,所以加上r.getId().equal(),后面开放其他城市就改动
     */
    public List<JSONObject> cascadeAllRegions() {
        List<Region> regions = allRegions();
        List<Region> provinces = simpleFilter(regions, r -> r.getRegionLevel().equals(1) /*&& r.getId().equals(101L)*/);
        Map<Long, List<Region>> citys = regions.stream()
                .filter(r -> r.getRegionLevel().equals(2) /*&& r.getId().equals(102L)*/)
                .collect(groupingBy(o -> o.getParent().getId()));

        Map<Long, List<Region>> areas = regions.stream()
                .filter(r -> r.getRegionLevel().equals(3))
                .collect(groupingBy(o -> o.getParent().getId()));

        return simpleMap(provinces, p -> wrapProvince(citys, areas, p));
    }

    private JSONObject wrapProvince(Map<Long, List<Region>> citys, Map<Long, List<Region>> areas, Region p) {
        JSONObject province = new JSONObject();
        province.put("id", p.getId());
        province.put("name", p.getName());
        province.put("list", simpleMap(citys.get(p.getId()), c -> wrapCity(areas, c)));//省所属的城市
        return province;
    }

    private JSONObject wrapCity(Map<Long, List<Region>> areas, Region c) {
        JSONObject city = new JSONObject();
        city.put("id", c.getId());
        city.put("name", c.getName());
        city.put("list", simpleMap(areas.get(c.getId()), this::wrapArea));//区;
        return city;
    }

    private JSONObject wrapArea(Region a) {
        JSONObject area = new JSONObject();
        area.put("id", a.getId());
        area.put("name", a.getName());
        return area;
    }

    /**
     * 过滤后的三级列表
     */
    public List<JSONObject> filterCascadeAllRegions(List<Region> chosedRegions) {
        List<Region> regions;
        if (isNotEmpty(chosedRegions))
            regions = regionRepository.findByIdNotIn(simpleFilterMap(chosedRegions, LambdaUtil::isNotNull, Region::getId));
        else
            regions = regionRepository.findAll();

        List<Region> provinces = simpleFilter(regions, r -> r.getRegionLevel().equals(1));
        Map<Long, List<Region>> citys = regions.stream()
                .filter(r -> r.getRegionLevel().equals(2))
                .collect(groupingBy(o -> o.getParent().getId()));
        // FIXME: 2017/1/12 目前只开放杭州市区域
//        List<Region> provinces = simpleFilter(regions, r -> r.getId().equals(101L));
//        Map<Long, List<Region>> citys = regions.stream()
//                .filter(r -> r.getId().equals(102L))
//                .collect(groupingBy(o -> o.getParent().getId()));

        Map<Long, List<Region>> areas = regions.stream()
                .filter(r -> r.getRegionLevel().equals(3))
                .collect(groupingBy(o -> o.getParent().getId()));

        return simpleMap(provinces, p -> wrapProvince(citys, areas, p));
    }

    public List<AppVersion> findAppVersions(PhoneType type) {
        return appVersionRepository.findByPhoneTypeOrderByUpdateDateDesc(type);
    }
}
