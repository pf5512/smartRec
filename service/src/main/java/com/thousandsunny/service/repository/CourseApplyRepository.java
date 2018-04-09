package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.CourseApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by 13336 on 2017/2/15.
 */
public interface CourseApplyRepository extends BaseRepository<CourseApply> {
    Page<CourseApply> findByMemberIdOrderByDateDesc(Long id, Pageable pageable);

    CourseApply findBySerialNoAndMemberId(String orderNo, Long id1);

    CourseApply findBySerialNo(String orderNo);

    long countByCourseId(Long id);

    List<CourseApply> findByMember(Member member);
}
