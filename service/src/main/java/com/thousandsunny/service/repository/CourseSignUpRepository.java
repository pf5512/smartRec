package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.CourseSignUp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.util.List;

/**
 * Created by 13336 on 2017/2/15.
 */
public interface CourseSignUpRepository extends BaseRepository<CourseSignUp> {
    List<CourseSignUp> findByCourseIdAndIsDelete(Long id, BooleanEnum no);

    List<CourseSignUp> findByCourseIdAndIsDeleteOrderByDate(Long id, BooleanEnum no);

    Page<CourseSignUp> findByCourseIdAndIsDelete(Long courseId, BooleanEnum no, Pageable pageable);

    CourseSignUp findByIdAndCourseId(Long id, Long courseId);

    CourseSignUp findByIdAndCourseIdAndIsDelete(Long id, Long courseId, BooleanEnum no);

    CourseSignUp findByCourseIdAndDateAndIsDeleteAndIsEnable(Long id, Date date, BooleanEnum no, BooleanEnum yes);
}
