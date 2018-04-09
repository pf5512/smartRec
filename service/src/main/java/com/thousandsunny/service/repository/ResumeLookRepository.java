package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.ResumeLook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by admin on 2016/10/27.
 */
public interface ResumeLookRepository extends BaseRepository<ResumeLook> {
    ResumeLook findTop1ByResumeMemberTokenOrderByDateDesc(String token);

    Long countByResumeMemberTokenAndIsRead(String token, BooleanEnum booleanEnum);

    List<ResumeLook> findByResumeMemberTokenAndIsReadOrderByDate(String token, BooleanEnum booleanEnum);

    Page<ResumeLook> findByResumeMemberTokenOrderByDateDesc(String token, Pageable pageable);

    ResumeLook findByShopOwnerTokenAndResumeMemberToken(String shopOwnerToken, String token);
    //int findByMemberToken();countByIdentifier
}
