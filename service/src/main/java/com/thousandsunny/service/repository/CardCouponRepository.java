package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey.CardCouponState;
import com.thousandsunny.service.model.CardCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by 13336 on 2017/2/13.
 */
public interface CardCouponRepository extends BaseRepository<CardCoupon> {
    Page<CardCoupon> findByStateAndShopIdAndIsDeleteOrderByRefreshTimeDesc(CardCouponState normal, Long id, BooleanEnum no, Pageable pageable);

    Page<CardCoupon> findByShopIdAndIsDeleteOrderByRefreshTimeDesc( Long id, BooleanEnum no, Pageable pageable);

    Integer countByShopIdAndIsDelete(Long id, BooleanEnum no);

    List<CardCoupon> findByStateAndIsDeleteAndIsStop(CardCouponState state,BooleanEnum flag,BooleanEnum top);
}
