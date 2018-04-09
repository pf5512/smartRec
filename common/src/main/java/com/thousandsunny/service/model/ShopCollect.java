package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
/**
 * Created by admin on 2016/10/25.
 */
@Data
@Entity
@Comment("店铺收藏")
@Table(name = "sr_shop_collect")
public class ShopCollect {
    private Long id;
    @Comment("店铺")
    private Shop shop;
    @Comment("收藏者")
    private Member member;
    @Comment("收藏时间")
    private Date date=new Date();
    @Comment("删除标记")
    private BooleanEnum collectEver = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public BooleanEnum getCollectEver() {
        return collectEver;
    }

    @OneToOne
    public Shop getShop(){return shop;}

    @OneToOne
    public Member getMember() {
        return member;
    }
}
