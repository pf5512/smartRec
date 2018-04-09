package com.thousandsunny.core.domain.repository;

import com.thousandsunny.common.entity.BackPageRequest;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.AppVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by mu.jie on 2016/9/26.
 */
public interface AppVersionRepository extends BaseRepository<AppVersion> {
    List<AppVersion> findByPhoneTypeOrderByUpdateDateDesc(ModuleKey.PhoneType type);

    Page<AppVersion> findByPhoneTypeOrderByUpdateDateDesc(ModuleKey.PhoneType type, Pageable pageable);

    Page<AppVersion> findByOrderByUpdateDateDesc(Pageable pageable);
}
