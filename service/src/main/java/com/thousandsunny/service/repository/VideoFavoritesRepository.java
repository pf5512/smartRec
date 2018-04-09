package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.VideoFavorites;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by admin on 2016/10/12.
 */
public interface VideoFavoritesRepository extends BaseRepository<VideoFavorites>{
    VideoFavorites findByMemberIdAndVideoId(Long memberId,Long videoId);
    Page<VideoFavorites> findByVideoIdAndFavoriteEver(Long videoId, ModuleKey.BooleanEnum favoriteEver, Pageable pageable);
    VideoFavorites findByMemberTokenAndVideoId(String userToken,Long videoId);
    List<VideoFavorites> findByVideoIdAndFavoriteEver(Long videoId, ModuleKey.BooleanEnum favoriteEver);
    List<VideoFavorites> findTop10ByVideoIdAndFavoriteEverOrderByFavoriteDate(Long id, ModuleKey.BooleanEnum favoriteEver);

    Integer countByVideoIdAndFavoriteEver(Long id, ModuleKey.BooleanEnum no);
}
