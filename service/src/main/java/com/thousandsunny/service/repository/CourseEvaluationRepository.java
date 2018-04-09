package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.CourseEvaluation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;

/**
 * Created by 13336 on 2017/2/14.
 */
public interface CourseEvaluationRepository extends BaseRepository<CourseEvaluation> {
    Page<CourseEvaluation> findByCourseIdInAndIsDeleteAndIsEnable(List<Long> courseIdList, BooleanEnum isDelete, BooleanEnum isEnable, Pageable pageable);

    CourseEvaluation findByMemberIdAndCourseIdAndIsDeleteAndIsEnable(Long memberId, Long id, BooleanEnum no, BooleanEnum yes);
}
