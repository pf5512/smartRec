package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.ShopCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
/**
 * Created by Thinkpad on 2016/11/1.
 */
public interface ShopCollectRepository extends BaseRepository<ShopCollect> {

    Page<ShopCollect> findByMemberTokenOrderByDateDesc(String userToken, Pageable pageable);

    ShopCollect findByMemberTokenAndShopId(String userToken, Long id);

    ShopCollect findByMemberTokenAndShopIdAndCollectEver(String userToken, Long id, BooleanEnum isCollectEver);
}
