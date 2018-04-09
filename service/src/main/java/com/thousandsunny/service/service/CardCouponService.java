package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.PageVO;
import com.thousandsunny.core.domain.repository.CloudFileRepository;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.CardCouponState;
import com.thousandsunny.service.model.CardCoupon;
import com.thousandsunny.service.model.CardCouponReceive;
import com.thousandsunny.service.model.Shop;
import com.thousandsunny.service.repository.CardCouponReceiveRepository;
import com.thousandsunny.service.repository.CardCouponRepository;
import com.thousandsunny.service.repository.ShopRepository;
import com.thousandsunny.thirdparty.ModuleKey;
import org.hibernate.jpa.criteria.OrderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Lists.newArrayList;
import static com.thousandsunny.common.DateUtil.dayGap;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.CardCouponState.NORMAL;
import static com.thousandsunny.service.ModuleKey.CardCouponState.PAUSE;
import static com.thousandsunny.service.ModuleKey.CardCouponType;
import static com.thousandsunny.service.ModuleTips.*;
import static com.thousandsunny.thirdparty.ModuleKey.OperatorType.SURE;

/**
 * Created by xiao.xue.wei on 2017/2/13.
 */
@Service
public class CardCouponService extends BaseService<CardCoupon> {
    @Autowired
    private CardCouponRepository cardCouponRepository;
    @Autowired
    private CardCouponReceiveRepository cardCouponReceiveRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private CloudFileRepository cloudFileRepository;

    public Page<CardCoupon> findCardOrCouponPage(Pageable pageable, Long provinceId, Long cityId, Long areaId, CardCouponType type) {
        Specification<CardCoupon> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("type"), type));
            predicates.add(rb.equal(rt.get("state"), NORMAL));
            predicates.add(rb.equal(rt.get("isStop"), NO));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotNullThen(provinceId, e -> predicates.add(rb.equal(rt.get("shop").get("province").get("id"), e)));
            ifNotNullThen(cityId, e -> predicates.add(rb.equal(rt.get("shop").get("city").get("id"), e)));
            ifNotNullThen(areaId, e -> predicates.add(rb.equal(rt.get("shop").get("area").get("id"), e)));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("refreshTime"), false)).getRestriction();
        };
        return cardCouponRepository.findAll(spec, pageable);
    }

    public void refresh(Member member, Long id) {
        CardCoupon cardCoupon = cardCouponRepository.findOne(id);
        ifFalseThrow(member.getToken().equals(cardCoupon.getShop().getOwner().getToken()), TIP_NO_AUTHORITY);
        Date now = new Date();
        if (dayGap(cardCoupon.getRefreshTime(), now) >= 1) {
            cardCoupon.setRefreshTime(now);
            cardCouponRepository.save(cardCoupon);
        } else {
            ifFalseThrow(false, TIP_ONE_DAY_ONE_TIMES);
        }
    }

    public void pause(Member member, Long id, ModuleKey.OperatorType operatorType) {
        CardCoupon cardCoupon = cardCouponRepository.findOne(id);
        ifFalseThrow(member.getToken().equals(cardCoupon.getShop().getOwner().getToken()), TIP_NO_AUTHORITY);
        if (operatorType == SURE) {
            cardCoupon.setIsStop(YES);
            cardCoupon.setState(PAUSE);
            cardCoupon.setStopTime(new Date());
        } else {
            cardCoupon.setIsStop(NO);
            cardCoupon.setState(NORMAL);
        }
        cardCouponRepository.save(cardCoupon);
    }

    public void deleteCard(Member member, Long id) {
        CardCoupon cardCoupon = cardCouponRepository.findOne(id);
        ifFalseThrow(member.getToken().equals(cardCoupon.getShop().getOwner().getToken()), TIP_NO_AUTHORITY);
        List<CardCouponReceive> cardCouponReceives = cardCouponReceiveRepository.findByCardCouponIdAndIsDelete(cardCoupon.getId(), NO);
        if (cardCouponReceives == null || cardCouponReceives.size() <= 0 || cardCoupon.getValidDate().getTime() < new Date().getTime()) {
            cardCoupon.setIsDelete(YES);
        } else {
            ifTrueThrow(true, TIP_COUPON_CARD_CANNOT_DELETE);
        }
        cardCouponRepository.save(cardCoupon);
    }

    public CardCoupon addOrEditCard(String userToken, CardCoupon cardCoupon) {
        Shop shop = shopRepository.findByOwnerToken(userToken);
        ifNullThrow(shop, TIP_SHOP_NOT_EXIST);
        ifNotNullThen(cardCoupon.getPhotos(), photos -> cardCoupon.setPhotos(cloudFileRepository.save(photos)));
        if (cardCoupon.getId() == null) {
            cardCoupon.setShop(shop);
            cardCoupon.setType(CardCouponType.CARD);
            return cardCouponRepository.save(cardCoupon);
        } else {
            List<CardCouponReceive> cardcouponReceives = cardCouponReceiveRepository.findByCardCouponIdAndIsDelete(cardCoupon.getId(), NO);
            ifTrueThrow(!cardcouponReceives.isEmpty(), TIP_COUPON_CARD_RECEIVED);
            CardCoupon oldCard = cardCouponRepository.findOne(cardCoupon.getId());
            ifNotNullThen(cardCoupon.getName(), x -> oldCard.setName(x));
            ifNotNullThen(cardCoupon.getDiscount(), x -> oldCard.setDiscount(x));
            ifNotNullThen(cardCoupon.getValidDate(), x -> oldCard.setValidDate(x));
            ifNotNullThen(cardCoupon.getDetails(), x -> oldCard.setDetails(x));
            ifNotNullThen(cardCoupon.getUseNotice(), x -> oldCard.setUseNotice(x));
            ifTrueThen(cardCoupon.getPhotos() != null && cardCoupon.getPhotos().size() > 0, () -> oldCard.setPhotos(cardCoupon.getPhotos()));
            oldCard.setShop(shop);
            return cardCouponRepository.save(oldCard);
        }
    }

    public CardCoupon addOrEditCoupon(String userToken, CardCoupon cardCoupon) {
        Shop shop = shopRepository.findByOwnerToken(userToken);
        ifNullThrow(shop, TIP_SHOP_NOT_EXIST);
        ifNotNullThen(cardCoupon.getPhotos(), photos -> cardCoupon.setPhotos(cloudFileRepository.save(photos)));
        if (cardCoupon.getId() == null) {
            cardCoupon.setShop(shop);
            cardCoupon.setType(CardCouponType.COUPON);
            return cardCouponRepository.save(cardCoupon);
        } else {
            List<CardCouponReceive> cardCouponReceives = cardCouponReceiveRepository.findByCardCouponIdAndIsDelete(cardCoupon.getId(), NO);
            ifTrueThrow(!cardCouponReceives.isEmpty(), TIP_COUPON_CARD_RECEIVED);
            CardCoupon oldCoupon = cardCouponRepository.findOne(cardCoupon.getId());
            ifNotNullThen(cardCoupon.getName(), x -> oldCoupon.setName(x));
            ifNotNullThen(cardCoupon.getCostPrice(), x -> oldCoupon.setCostPrice(x));
            ifNotNullThen(cardCoupon.getSalePrice(), x -> oldCoupon.setSalePrice(x));
            ifNotNullThen(cardCoupon.getValidDate(), x -> oldCoupon.setValidDate(x));
            ifNotNullThen(cardCoupon.getCanReceiveManyTimes(), x -> oldCoupon.setCanReceiveManyTimes(x));
            ifNotNullThen(cardCoupon.getDetails(), x -> oldCoupon.setDetails(x));
            ifNotNullThen(cardCoupon.getUseNotice(), x -> oldCoupon.setUseNotice(x));
            ifTrueThen(cardCoupon.getPhotos() != null && cardCoupon.getPhotos().size() > 0, () -> oldCoupon.setPhotos(cardCoupon.getPhotos()));
            oldCoupon.setShop(shop);
            return cardCouponRepository.save(oldCoupon);
        }
    }

    public CardCouponReceive findCardCouponReceive(Member member, Long id) {
        return cardCouponReceiveRepository.findTop1ByMemberIdAndCardCouponIdAndIsDeleteOrderByReceiveTimeDesc(member.getId(), id, NO);
    }

    public Page<CardCoupon> findNormalCardCoupon(Long id, PageVO pageVO) {
        return cardCouponRepository.findByStateAndShopIdAndIsDeleteOrderByRefreshTimeDesc(NORMAL, id, NO, pageVO.pageRequest());
    }

    public Page<CardCoupon> findMyNormalCardCoupon(String userToken, Long id, PageVO pageVO) {
        Shop shop = shopRepository.findByOwnerToken(userToken);
        ifTrueThrow(shop == null || !id.equals(shop.getId()), TIP_NO_AUTHORITY);
        return cardCouponRepository.findByShopIdAndIsDeleteOrderByRefreshTimeDesc(id, NO, pageVO.pageRequest());
    }

    /**
     * 卡劵是否被领取
     *
     * @Author mu.jie
     * @Date 2017/2/14
     */
    public Boolean isReceived(CardCoupon cardCoupon) {
        List<CardCouponReceive> couponReceiveList = cardCouponReceiveRepository.findByCardCouponIdAndIsDelete(cardCoupon.getId(), NO);
        if (couponReceiveList != null && couponReceiveList.size() > 0) return true;
        else return false;
    }

    public Integer countShopCardCoupons(Shop shop) {
        Integer num = cardCouponRepository.countByShopIdAndIsDelete(shop.getId(), NO);
        if (isNotNull(num)) return num;
        else return 0;
    }

    public Page<CardCoupon> findcardCouponPage(Pageable pageable, CardCouponType cardVoucherType,
                                               CardCouponState cardVoucherStatus, Long shopId, String text) {
        Specification<CardCoupon> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("shop").get("id"), shopId));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            ifNotNullThen(cardVoucherType, e -> predicates.add(rb.equal(rt.get("type"), e)));
            ifNotNullThen(cardVoucherStatus, e -> predicates.add(rb.equal(rt.get("state"), e)));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("name"), "%" + e + "%")));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("refreshTime"), false)).getRestriction();
        };
        return cardCouponRepository.findAll(spec, pageable);
    }

    public void enableCardCoupon(Long id) {
        CardCoupon cardCoupon = cardCouponRepository.findOne(id);
        ifNullThrow(cardCoupon, TIP_CARD_NOT_EXIST);
        if(cardCoupon.getIsEnable() == YES) cardCoupon.setIsEnable(NO);
        else cardCoupon.setIsEnable(YES);
    }
}
