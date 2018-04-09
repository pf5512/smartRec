package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.ApplyState;
import static com.thousandsunny.core.ModuleKey.ApplyState.APPLY;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static java.lang.Boolean.TRUE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在18/11/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("转让记录")
@Table(name = "sr_transfer_record")
public class TransferRecord {

    private Long id;
    @Comment("商铺店名")
    private String shopName;
    @Comment("店铺")
    private Shop shop;
    @Comment("转让人")
    private Member assignor;
    @Comment("对方姓名")
    private String receiverRealName;
    @Comment("对方手机号码")
    private String receiverPhoneNumber;
    @Comment("对方慧聘帐号")
    private String receiverHpAccount;
    @Comment("转让原因")
    private String excuse;
    @Comment("日期")
    private Date date = new Date();
    @Comment("备注")
    private String remark;
    @Comment("申请状态")
    private ApplyState state = APPLY;
    private Boolean valid = TRUE;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;
    @Comment("审核时间")
    private Date reviewTime;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Column(columnDefinition = TEXT)
    public String getExcuse() {
        return excuse;
    }

    @Column(columnDefinition = TEXT)
    public String getRemark() {
        return remark;
    }

    @Enumerated(STRING)
    public ApplyState getState() {
        return state;
    }

    @OneToOne
    public Member getAssignor() {
        return assignor;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Shop getShop() {
        return shop;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }
}
