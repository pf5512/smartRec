package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.ResumeCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/10/27.
 */
public interface ResumeCollectRepository extends BaseRepository<ResumeCollect> {
    ResumeCollect findByMemberTokenAndResumeId(String userToken,Long id);

    ResumeCollect findByMemberTokenAndResumeMemberTokenAndCollectEver(String userToken,String checkedUserToken,BooleanEnum no);

    Page<ResumeCollect> findByMemberTokenAndCollectEverOrderByDateDesc(String userToken, BooleanEnum no, Pageable pageable);
}
