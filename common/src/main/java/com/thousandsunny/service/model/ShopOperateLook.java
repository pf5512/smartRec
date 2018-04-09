package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;

import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by 13336 on 2017/1/13.
 */
@Data
@Entity
@Comment("店铺合作/转让---用户查看中间表")
@Table(name = "sr_shop_operate_look")
public class ShopOperateLook {

    private Long id;
    @Comment("店铺合作/转让")
    private ShopOperate shopOperate;
    @Comment("用户")
    private Member member;
    @Comment("是否已读")
    private BooleanEnum isRead = NO;
    @Comment("读取时间")
    private Date readTime = new Date();

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public ShopOperate getShopOperate() {
        return shopOperate;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsRead() {
        return isRead;
    }
}
