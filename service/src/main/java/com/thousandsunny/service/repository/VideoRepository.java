package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Created by admin on 2016/10/12.
 */
public interface VideoRepository extends BaseRepository<Video>{
    Page<Video> findByTitleContainingOrContentContaining(String title,String name,Pageable pageable);
    Page<Video> findAll(Pageable pageable);

    Page<Video> findByIsDeleteOrderByDateDesc(ModuleKey.BooleanEnum no, Pageable pageable);
}
