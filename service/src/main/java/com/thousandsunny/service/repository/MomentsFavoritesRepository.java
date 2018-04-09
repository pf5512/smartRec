package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.MomentsFavorites;

import java.util.List;

/**
 * Created by admin on 2016/10/14.
 */
public interface MomentsFavoritesRepository extends BaseRepository<MomentsFavorites>{
    MomentsFavorites findByMemberTokenAndMomentsId(String token,Long momentsId);
    List<MomentsFavorites> findByMomentsIdAndFavoriteEver(Long id, ModuleKey.BooleanEnum favoriteEver);
    Integer countByMomentsIdAndFavoriteEver(Long id,ModuleKey.BooleanEnum favoriteEver);
}
