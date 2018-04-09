package com.thousandsunny.service.repository;

import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.BaseRepository;
import com.thousandsunny.service.ModuleKey.CardCouponReceiveState;
import com.thousandsunny.service.model.CardCouponReceive;

import java.util.List;

/**
 * Created by 13336 on 2017/2/13.
 */
public interface CardCouponReceiveRepository extends BaseRepository<CardCouponReceive> {
    CardCouponReceive findTop1ByMemberIdAndCardCouponIdAndIsDeleteOrderByReceiveTimeDesc(Long memberId, Long id, ModuleKey.BooleanEnum no);

    List<CardCouponReceive> findByCardCouponIdAndIsDelete(Long id, ModuleKey.BooleanEnum isDelete);

    List<CardCouponReceive> findByCardCouponIdAndStateAndIsDelete(Long id,CardCouponReceiveState state, ModuleKey.BooleanEnum isDelete);

    CardCouponReceive findByCardCouponCodeAndMemberId(String cardBarCode,Long memberId);

    CardCouponReceive findByCardCouponCode(String cardBarCode);

    Long countByMemberIdAndStateAndIsOverdueAndIsDelete(Long id, CardCouponReceiveState received, ModuleKey.BooleanEnum no, ModuleKey.BooleanEnum no1);
}
