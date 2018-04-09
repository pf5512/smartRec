package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.VideoCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/10/13.
 */
public interface VideoCollectRepository extends BaseRepository<VideoCollect>{
    VideoCollect findByMemberTokenAndVideoId(String userToken,Long videoId);

    Page<VideoCollect> findByMemberTokenAndCollectEverOrderByDateDesc(String userToken, BooleanEnum no, Pageable pageable);
}
