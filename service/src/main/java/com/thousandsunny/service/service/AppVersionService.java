package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageVo;
import com.thousandsunny.core.ModuleKey.PhoneType;
import com.thousandsunny.core.domain.repository.AppVersionRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.AppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.thousandsunny.common.lambda.LambdaUtil.ifNotBlankThen;
import static jersey.repackaged.com.google.common.collect.Lists.newArrayList;

/**
 * Created by mu.jie on 2017/1/16.
 */
@Service
public class AppVersionService extends BaseService<AppVersion> {
    @Autowired
    private AppVersionRepository appVersionRepository;

    public Page<AppVersion> findAppVersionList(BackPageVo backPageVo, PhoneType phoneType) {
        if (phoneType == null)
            return appVersionRepository.findByOrderByUpdateDateDesc(backPageVo.pageRequest());
        return appVersionRepository.findByPhoneTypeOrderByUpdateDateDesc(phoneType, backPageVo.pageRequest());
    }

    public String delAppVersion(String ids) {
        ifNotBlankThen(ids, idArr -> newArrayList(ids.split(",")).forEach(x -> {
            AppVersion app = appVersionRepository.findOne(Long.parseLong(x));
            appVersionRepository.delete(app);
        }));
        return "success";
    }

    public void addAppVersion(PhoneType type, String version, String lowestVersion) {
        AppVersion app = new AppVersion();
        app.setMinVersion(lowestVersion);
        app.setPhoneType(type);
        app.setUpdateDate(new Date());
        app.setVersion(version);
        appVersionRepository.save(app);
    }
}
