package com.thousandsunny.cms.domain.service;

import com.thousandsunny.cms.domain.repository.CmsTagRepository;
import com.thousandsunny.cms.model.CmsTag;
import com.thousandsunny.core.domain.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.thousandsunny.common.lambda.LambdaUtil.*;

import java.util.List;

import static com.thousandsunny.cms.ModuleKey.TagType.COURSE;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.cms.ModuleTips.TIP_NO_TAG;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

@Service
public class CmsTagService extends BaseService<CmsTag> {
    @Autowired
    private CmsTagRepository cmsTagRepository;

    public List<CmsTag> findCourseTagList() {
        return cmsTagRepository.findByTypeAndIsDelete(COURSE, NO);
    }

    public void editTag(Long id, String text) {
        CmsTag cmsTag;
        if (isNotNull(id)) {
            cmsTag = cmsTagRepository.findOne(id);
            ifNullThrow(cmsTag, TIP_NO_TAG);
        } else cmsTag = new CmsTag();
        ifNotBlankThen(text, e -> cmsTag.setName(e));
        cmsTag.setIsDelete(NO);
        cmsTag.setType(COURSE);
        cmsTagRepository.save(cmsTag);
    }

    public void deleteTags(Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            CmsTag cmsTag = cmsTagRepository.findOne(ids[i]);
            ifNullThrow(cmsTag, TIP_NO_TAG);
            cmsTag.setIsDelete(YES);
            cmsTagRepository.save(cmsTag);
        }
    }
}
