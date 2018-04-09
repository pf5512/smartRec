package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.service.ModuleKey.CardCouponReceiveState.RECEIVED;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static com.thousandsunny.service.ModuleKey.CardCouponReceiveState;

/**
 * Created by 13336 on 2017/2/13.
 */
@Data
@Entity
@Comment("卡/券_用户领取表")
@Table(name = "sr_card_coupon_receive")
public class CardCouponReceive {
    private Long id;
    @Comment("用户")
    private Member member;
    @Comment("卡/券")
    private CardCoupon cardCoupon;
    @Comment("是否过期")
    private ModuleKey.BooleanEnum isOverdue = NO;
    @Comment("是否删除")
    private ModuleKey.BooleanEnum isDelete = NO;
    @Comment("卡/券条码")
    private String cardCouponCode;
    @Comment("领取状态")
    private CardCouponReceiveState state = RECEIVED;
    @Comment("领取时间")
    private Date receiveTime = new Date();
    // 以下为券所有
    @Comment("券是否使用")
    private ModuleKey.BooleanEnum isUse = NO;
    @Comment("使用时间")
    private Date useTime;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return this.id;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getIsUse() {
        return isUse;
    }

    @Enumerated(STRING)
    public CardCouponReceiveState getState() {
        return state;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getIsOverdue() {
        return isOverdue;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getIsDelete() {
        return isDelete;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = LAZY)
    public CardCoupon getCardCoupon() {
        return cardCoupon;
    }

}
