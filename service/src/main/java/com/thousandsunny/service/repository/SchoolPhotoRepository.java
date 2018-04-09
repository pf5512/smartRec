package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.SchoolPhoto;
import com.thousandsunny.service.ModuleKey.PhotoType;

import java.util.List;

/**
 * Created by 13336 on 2017/2/16.
 */
public interface SchoolPhotoRepository extends BaseRepository<SchoolPhoto> {
    SchoolPhoto findTop1BySchoolIdAndTypeAndIsEnableAndIsDeleteOrderByNumber(Long id, PhotoType type, BooleanEnum isEnable,BooleanEnum no);

    List<SchoolPhoto> findBySchoolIdAndTypeAndIsEnableAndIsDeleteOrderByNumber(Long id, PhotoType environment, BooleanEnum isEnable, BooleanEnum isDelete);

    SchoolPhoto findByIdAndIsDelete(Long id, BooleanEnum no);

    List<SchoolPhoto> findBySchoolIdAndIsDelete(Long id, BooleanEnum no);
}
