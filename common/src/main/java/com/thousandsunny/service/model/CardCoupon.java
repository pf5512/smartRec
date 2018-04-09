package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.CardCouponState.NORMAL;
import static com.thousandsunny.service.ModuleKey.CardCouponState;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static javax.persistence.TemporalType.DATE;
import static com.thousandsunny.service.ModuleKey.CardCouponType;

/**
 * Created by 13336 on 2017/2/13.
 */
@Data
@Entity
@Comment("优惠券/卡")
@Table(name = "sr_card_coupon")
public class CardCoupon {
    private Long id;
    @Comment("名称")
    private String name;

    //折扣为卡所有
    @Comment("折扣")
    private Double discount;
    @Comment("最后有效时间")
    private Date validDate;
    @Comment("创建时间")
    private Date createTime = new Date();
    @Comment("刷新时间")
    private Date refreshTime = new Date();
    @Comment("商品详情")
    private String details;
    @Comment("使用须知")
    private String useNotice;
    @Comment("店铺")
    private Shop shop;
    @Comment("图片集")
    private List<CloudFile> photos;
    @Comment("暂停时间")
    private Date stopTime;
    @Comment("是否暂停")
    private BooleanEnum isStop = NO;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;
    @Comment("状态")
    private CardCouponState state = NORMAL;

    // 原价，优惠劵价格，是否允许多次领取 为优惠券所有
    @Comment("原价")
    private BigDecimal costPrice;
    @Comment("优惠后价格")
    private BigDecimal salePrice;
    @Comment("是否允许多次领取")
    private BooleanEnum canReceiveManyTimes = NO;
    @Comment("卡/券 类型")
    private CardCouponType type;
    @Comment("是否后台启用")
    private BooleanEnum isEnable = YES;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return this.id;
    }

    @Enumerated(STRING)
    public BooleanEnum getCanReceiveManyTimes() {
        return canReceiveManyTimes;
    }

    @Enumerated(STRING)
    public CardCouponState getState() {
        return state;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsStop() {
        return isStop;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsEnable() {
        return isEnable;
    }

    @Enumerated(STRING)
    public CardCouponType getType() {
        return type;
    }

    @Transient
    public Boolean getIsEnableBoolean() {
        return isEnable == YES;
    }

    @OneToMany
    public List<CloudFile> getPhotos() {
        return photos;
    }

    @Temporal(DATE)
    public Date getValidDate() {
        return validDate;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @Transient
    public Boolean getCanReceiveManyTimesBoolean() {
        return canReceiveManyTimes == YES;
    }
}
