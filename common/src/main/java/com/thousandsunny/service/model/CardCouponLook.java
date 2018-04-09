package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by 13336 on 2017/2/13.
 */
@Data
@Entity
@Comment("卡/券_用户是否已读")
@Table(name = "sr_card_coupon_look")
public class CardCouponLook {
    private Long id;
    @Comment("卡/券")
    private CardCoupon cardCoupon;
    @Comment("用户")
    private Member member;
    @Comment("是否已读")
    private ModuleKey.BooleanEnum isRead = NO;
    @Comment("读取时间")
    private Date readTime = new Date();

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return this.id;
    }

    @OneToOne(fetch = LAZY)
    public CardCoupon getCardCoupon() {
        return cardCoupon;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getIsRead() {
        return isRead;
    }
}
