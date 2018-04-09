package com.thousandsunny.service.repository;

import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.ShopOperate;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.service.ModuleKey.OperateType;

/**
 * Created by 13336 on 2017/1/13.
 */
public interface ShopOperateRepository extends BaseRepository<ShopOperate> {

    ShopOperate findByTypeAndShopId(OperateType operateType, Long shopId);

    Long countByShopIdAndTypeAndIsOpen(Long shopId, OperateType transfer, BooleanEnum yes);
}
