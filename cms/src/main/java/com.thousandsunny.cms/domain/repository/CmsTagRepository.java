package com.thousandsunny.cms.domain.repository;


import com.thousandsunny.cms.ModuleKey.TagType;
import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.ModuleKey.BooleanEnum;

import java.util.List;

public interface CmsTagRepository extends BaseRepository<CmsTag> {
    List<CmsTag> findByTypeAndIsDelete(TagType course, BooleanEnum no);

    CmsTag findByIdAndIsDelete(Long courseClassId, BooleanEnum no);
}
