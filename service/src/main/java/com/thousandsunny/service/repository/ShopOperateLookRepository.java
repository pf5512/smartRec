package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.ShopOperateLook;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.service.ModuleKey.OperateType;

/**
 * Created by 13336 on 2017/1/13.
 */
public interface ShopOperateLookRepository extends BaseRepository<ShopOperateLook> {
    Long countByMemberTokenAndShopOperateIdAndIsRead(String token, Long id, BooleanEnum isRead);

    ShopOperateLook findByMemberTokenAndShopOperateId(String memberToken, Long shopOperateId);
}
