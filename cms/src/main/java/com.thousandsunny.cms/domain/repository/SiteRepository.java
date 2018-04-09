package com.thousandsunny.cms.domain.repository;


import com.thousandsunny.cms.model.Site;
import com.thousandsunny.core.domain.repository.BaseRepository;

import java.util.List;

import static com.thousandsunny.cms.ModuleKey.SiteTypeEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
public interface SiteRepository extends BaseRepository<Site> {
    List<Site> findByEntityStatusAndIsDelete(BooleanEnum entityStatus,BooleanEnum no);

    List<Site> findByIsDeleteAndTypeIn(BooleanEnum no,List<SiteTypeEnum> siteType);
}
