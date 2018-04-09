package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.model.CardCouponLook;

/**
 * Created by 13336 on 2017/2/13.
 */
public interface CardCouponLookRepository extends BaseRepository<CardCouponLook> {
    CardCouponLook findByMemberTokenAndCardCouponIdAndIsRead(String token, Long id, ModuleKey.BooleanEnum isRead);

    CardCouponLook findByMemberTokenAndCardCouponId(String token, Long id);
}
