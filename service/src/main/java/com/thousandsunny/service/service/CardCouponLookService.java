package com.thousandsunny.service.service;

import com.thousandsunny.core.domain.service.BaseService;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.*;
import com.thousandsunny.service.repository.CardCouponLookRepository;
import com.thousandsunny.service.repository.CardCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.thousandsunny.common.lambda.LambdaUtil.ifNullThrow;
import static com.thousandsunny.common.lambda.LambdaUtil.isNotNull;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleTips.TIP_COUPON_NOT_EXIST;

/**
 * Created by 13336 on 2017/2/13.
 */
@Service
public class CardCouponLookService extends BaseService<CardCouponLook> {
    @Autowired
    private CardCouponLookRepository cardCouponLookRepository;
    @Autowired
    private CardCouponRepository cardCouponRepository;

    /**
     * 判断是否已读
     *
     * @Author xiao xue wei
     * @Date 2017/1/17
     */
    public boolean judgeIsRead(Member member, CardCoupon cardCoupon) {
        CardCouponLook cardCouponLook = cardCouponLookRepository.findByMemberTokenAndCardCouponIdAndIsRead(member.getToken(), cardCoupon.getId(), YES);
        if (isNotNull(cardCouponLook)) return true;
        else return false;
    }

    public void setToRead(Member member, Long id) {
        CardCouponLook cardCouponLook = cardCouponLookRepository.findByMemberTokenAndCardCouponId(member.getToken(), id);
        if (!isNotNull(cardCouponLook)) {
            CardCoupon cardCoupon = cardCouponRepository.findOne(id);
            ifNullThrow(cardCoupon, TIP_COUPON_NOT_EXIST);
            cardCouponLook = new CardCouponLook();
            cardCouponLook.setMember(member);
            cardCouponLook.setCardCoupon(cardCoupon);
        }
        cardCouponLook.setIsRead(YES);
        cardCouponLookRepository.save(cardCouponLook);
    }

}
