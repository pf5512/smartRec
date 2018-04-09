package com.thousandsunny.service.model;


import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 6/24/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Entity
@Comment("店铺点赞列表")
@Table(name = "sr_shop_favorite")
public class ShopFavorites {
    private Long id;
    @Comment("店铺")
    private Shop shop;
    @Comment("点赞时间")
    private Date favoriteDate;
    @Comment("点赞者")
    private Member member;
    private BooleanEnum favoriteEver;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public BooleanEnum getFavoriteEver() {
        return favoriteEver;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

}
