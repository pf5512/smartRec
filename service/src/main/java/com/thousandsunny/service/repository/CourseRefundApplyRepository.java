package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.CourseApply;
import com.thousandsunny.service.model.CourseRefundApply;

/**
 * Created by mu.jie on 2017/2/17.
 */
public interface CourseRefundApplyRepository extends BaseRepository<CourseRefundApply> {
    CourseRefundApply findByMemberAndCourseApply(Member member, CourseApply courseApply);
}
