package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.SchoolCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by 13336 on 2017/2/14.
 */
public interface SchoolCollectRepository extends BaseRepository<SchoolCollect> {
    SchoolCollect findByMemberTokenAndSchoolIdAndCollectEver(String token, Long id, ModuleKey.BooleanEnum no);

    SchoolCollect findByMemberTokenAndSchoolId(String token, Long schoolId);

    Page<SchoolCollect> findByMemberTokenAndCollectEverOrderByDateDesc(String userToken, ModuleKey.BooleanEnum no, Pageable pageable);
}
