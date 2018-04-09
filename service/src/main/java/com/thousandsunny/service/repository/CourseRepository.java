package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.Course;

import java.util.List;

/**
 * Created by mu.jie on 2016/11/24.
 */
public interface CourseRepository extends BaseRepository<Course> {
    List<Course> findBySchoolIdAndIsDeleteAndIsEnable(Long id, BooleanEnum no, BooleanEnum isEnable);
}
