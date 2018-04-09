package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.service.ModuleKey.OperateType;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;

/**
 * Created by 13336 on 2017/1/13.
 */
@Data
@Entity
@Comment("店铺合作/转让申请")
@Table(name = "sr_shop_operate")
public class ShopOperate {
    @Comment("id")
    private Long id;
    @Comment("店铺")
    private Shop shop;
    @Comment("发起时间")
    private Date launchTime = new Date();
    @Comment("店铺面积")
    private BigDecimal shopSquare;
    @Comment("店铺租金")
    private BigDecimal shopRental;
    @Comment("刷新时间")
    private Date freshTime = new Date();
    @Comment("是否开启")
    private BooleanEnum isOpen = YES;
    @Comment("合伙/转让？")
    private OperateType type;
    @Comment("备注")
    private String remark;
    @Comment("关闭时间")
    private Date closeTime;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsOpen() {
        return isOpen;
    }

    @Transient
    public Boolean getIsOpenBoolean(){
        return isOpen == YES;
    }

    @Enumerated(STRING)
    public OperateType getType() {
        return type;
    }

}
