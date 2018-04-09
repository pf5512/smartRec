package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.JobType;
import com.thousandsunny.service.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Created by admin on 2016/10/25.
 */
public interface ResumeRepository extends BaseRepository<Resume> {

    Page<Resume> findAllByOrderByFreshDescModifyDesc(Pageable pageable);

    Resume findByMemberToken(String userToken);

    @Query(value = "select * from sr_resume s order by round(6378.138*2*asin(sqrt(pow(sin((:lat*pi()/180-s.latitude*pi()/180)/2),2)+" +
            "cos(:lat*pi()/180)*cos(s.latitude*pi()/180)*pow(sin((:lon*pi()/180-s.longitude*pi()/180)/2),2)))*1000) limit :offset,:pageSize", nativeQuery = true)
    List<Resume> findByDistance(@Param("lon") Double longitude, @Param("lat") Double latitude, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);


    @Query(value = "select s.* from sr_resume s left join sr_resume_intention sr on  sr.id = s.intention_id " +
            " LEFT JOIN sr_job_constant sj ON sj.id = s.period_id  where sr.type like :type" +
            " AND sr.salary BETWEEN :salaryMin and :salaryMax  AND sr.work_year BETWEEN :experienceMin and :experienceMax  and s.intention_id is not null" +
            " AND s.longitude is not null AND s.latitude is not null order by sr.find_job_state asc ,round(6378.138*2*asin(sqrt(pow(sin((:lat*pi()/180-s.latitude*pi()/180)/2),2)+ " +
            " cos(:lat*pi()/180)*cos(s.latitude*pi()/180)*pow(sin((:lon*pi()/180-s.longitude*pi()/180)/2),2)))*1000) limit :offset,:pageSize", nativeQuery = true)
    List<Resume> findByDistance1(@Param("type") String jobType, @Param("salaryMin") Integer min, @Param("salaryMax") Integer max,
                                 @Param("experienceMin") Integer experienceMin, @Param("experienceMax") Integer experienceMax,
                                 @Param("lon") Double longitude, @Param("lat") Double latitude, @Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize);

    Long countByMemberId(Long id);

    Integer countByMemberIdInAndIntentionIsNotNull(List<Long> memberIds);
}
