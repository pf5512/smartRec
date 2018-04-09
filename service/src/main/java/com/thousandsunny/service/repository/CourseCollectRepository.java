package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.CourseCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by 13336 on 2017/2/15.
 */
public interface CourseCollectRepository extends BaseRepository<CourseCollect> {
    CourseCollect findByMemberTokenAndCourseIdAndCollectEver(String token, Long id, BooleanEnum no);

    CourseCollect findByMemberTokenAndCourseId(String token, Long id);

    Page<CourseCollect> findByMemberTokenAndCollectEverOrderByDateDesc(String userToken,BooleanEnum no, Pageable pageable);
}
