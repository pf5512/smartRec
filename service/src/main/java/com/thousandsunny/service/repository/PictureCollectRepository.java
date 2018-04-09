package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.PictureCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/10/27.
 */
public interface PictureCollectRepository extends BaseRepository<PictureCollect>{
    PictureCollect findByMemberTokenAndOwnerTokenAndPicturePath(String member,String owner,String path);

    Page<PictureCollect> findByMemberTokenAndCollectEverOrderByDateDesc(String userToken, BooleanEnum no, Pageable pageable);

}
