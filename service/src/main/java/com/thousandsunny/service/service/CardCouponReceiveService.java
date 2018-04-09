package com.thousandsunny.service.service;

import com.thousandsunny.common.entity.BackPageRequest;
import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.model.CardCoupon;
import com.thousandsunny.service.model.CardCouponReceive;
import com.thousandsunny.service.repository.CardCouponReceiveRepository;
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
import static com.thousandsunny.common.RandomNumberUtil.getOrderNo;
import static com.thousandsunny.common.lambda.LambdaUtil.*;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.CardCouponReceiveState.*;
import static com.thousandsunny.service.ModuleKey.CardCouponState.*;
import static com.thousandsunny.service.ModuleTips.*;

/**
 * Created by 13336 on 2017/2/13.
 */
@Service
public class CardCouponReceiveService extends BaseService<CardCouponReceive> {
    @Autowired
    private CardCouponReceiveRepository cardCouponReceiveRepository;

    /**
     * 查看领取状态
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    public ModuleKey.CardCouponReceiveState viewReceiveStatus(Member member, CardCoupon cardCoupon) {
        CardCouponReceive cardCouponReceive = cardCouponReceiveRepository.findTop1ByMemberIdAndCardCouponIdAndIsDeleteOrderByReceiveTimeDesc(member.getId(), cardCoupon.getId(), NO);
        if (isNotNull(cardCouponReceive)) return cardCouponReceive.getState();
        else return NOT_RECEIVING;
    }

    public List<CardCouponReceive> findMyCardCouponPage(Member member, String isGetNormalState, ModuleKey.CardCouponType type) {
        Specification<CardCouponReceive> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("cardCoupon").get("type"), type));
            ifNotBlankThen(isGetNormalState, e -> {
                if ("YES".equals(e)) predicates.add(rb.equal(rt.get("cardCoupon").get("state"), NORMAL));
            });
            predicates.add(rb.equal(rt.get("member"), member));
            predicates.add(rb.equal(rt.get("cardCoupon").get("isDelete"), NO));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("receiveTime"), false)).getRestriction();
        };
        return cardCouponReceiveRepository.findAll(spec);
    }


    public void createCardCouponReceive(Member member, CardCoupon cardCoupon) {
        CardCouponReceive oldCardCouponReceive = cardCouponReceiveRepository.findTop1ByMemberIdAndCardCouponIdAndIsDeleteOrderByReceiveTimeDesc(member.getId(), cardCoupon.getId(), NO);
        ifTrueThrow(oldCardCouponReceive != null && cardCoupon.getCanReceiveManyTimes() == NO, TIP_COUPON_CARD_CANNOT_RECEIVED_AGAIN);
        CardCouponReceive cardCouponReceive = new CardCouponReceive();
        cardCouponReceive.setMember(member);
        cardCouponReceive.setCardCouponCode(getOrderNo());
        cardCouponReceive.setState(RECEIVED);
        cardCouponReceive.setCardCoupon(cardCoupon);
        cardCouponReceiveRepository.save(cardCouponReceive);
    }

    public void verify(Member shopManager, Member member, String cardBarCode) {
        CardCouponReceive cardCouponReceive = cardCouponReceiveRepository.findByCardCouponCodeAndMemberId(cardBarCode, member.getId());
        ifNullThrow(cardCouponReceive, TIPS_NOT_HAS_CARD_COUPON);
        ifFalseThrow(shopManager.getId().equals(cardCouponReceive.getCardCoupon().getShop().getOwner().getId()), TIPS_SHOP_CARD_COUPON_NOT_EXIST);
        ifTrueThrow(cardCouponReceive.getCardCoupon().getValidDate().getTime() < new Date().getTime(), TIPS_CARD_COUPON_OUT_OF_DATE);
        cardCouponReceive.setState(RECEIVED_USED);
        cardCouponReceiveRepository.save(cardCouponReceive);
    }

    public CardCouponReceive storeManagerLookCardCoupon(Member shopManager, String cardCode) {
        CardCouponReceive cardCouponReceive = cardCouponReceiveRepository.findByCardCouponCode(cardCode);
        ifNullThrow(cardCouponReceive, TIPS_NOT_HAS_CARD_COUPON);
        ifFalseThrow(shopManager.getId().equals(cardCouponReceive.getCardCoupon().getShop().getOwner().getId()), TIPS_SHOP_CARD_COUPON_NOT_EXIST);
        return cardCouponReceive;
    }

    public Page<CardCouponReceive> findShopCardCouponReceivePage(Pageable pageable, String text, String useStatus, Long cardVoucherId) {
        Specification<CardCouponReceive> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("cardCoupon").get("id"), cardVoucherId));
            ifNotBlankThen(text, e -> predicates.add(rb.or(rb.like(rt.get("member").get("realName"), "%" + e + "%"),
                    rb.like(rt.get("member").get("mobile"), "%" + e + "%"))));
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            if ("nomal".equals(useStatus)) {
                predicates.add(rb.equal(rt.get("isOverdue"), NO));
            } else if ("used".equals(useStatus)) {
                predicates.add(rb.equal(rt.get("cardCoupon").get("type"), ModuleKey.CardCouponType.COUPON));
                predicates.add(rb.equal(rt.get("isUse"), YES));
            } else if ("overdue".equals(useStatus)) {
                predicates.add(rb.equal(rt.get("isOverdue"), YES));
            }
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("receiveTime"), false)).getRestriction();
        };
        return cardCouponReceiveRepository.findAll(spec, pageable);
    }

    public Page<CardCouponReceive> findMembersCardCouponReceive
            (Pageable pageable, String text, ModuleKey.CardCouponType type, String status, Member member) {
        Specification<CardCouponReceive> spec = (rt, rq, rb) -> {
            List<Predicate> predicates = newArrayList();
            predicates.add(rb.equal(rt.get("member"), member));
            ifNotBlankThen(text, e -> predicates.add(rb.like(rt.get("cardCoupon").get("name"), "%" + e + "%")));
            ifNotNullThen(type, e -> predicates.add(rb.equal(rt.get("cardCoupon").get("type"), e)));
            if ("nomal".equals(status)) {
                predicates.add(rb.equal(rt.get("isOverdue"), NO));
            } else if ("used".equals(status)) {
                predicates.add(rb.equal(rt.get("cardCoupon").get("type"), ModuleKey.CardCouponType.COUPON));
                predicates.add(rb.equal(rt.get("isUse"), YES));
            } else if ("overdue".equals(status)) {
                predicates.add(rb.equal(rt.get("isOverdue"), YES));
            }
            predicates.add(rb.equal(rt.get("isDelete"), NO));
            return rq.where(toArray(predicates, Predicate.class)).orderBy(new OrderImpl(rt.get("receiveTime"), false)).getRestriction();
        };
        return cardCouponReceiveRepository.findAll(spec, pageable);
    }

    public Long countMyCardcoupon(Member member) {
        return cardCouponReceiveRepository.countByMemberIdAndStateAndIsOverdueAndIsDelete(member.getId(), RECEIVED, NO, NO);
    }
}
