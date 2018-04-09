package com.thousandsunny.thirdparty.model;


import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.thirdparty.ModuleKey.*;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

@Data
@MappedSuperclass
public class BaseAccountApplyRecord {
    @Comment("会员")
    private Member member;
    @Comment("订单号")
    private String orderNo;
    @Comment("充值账户")
    private Account account;
    @Comment("金额")
    private BigDecimal amount;
    @Comment("账户类型")
    private PayType payType;
    @Comment("创建时间")
    private Date createDate;
    @Comment("充值状态")
    private FlowState state;
    @Comment("记录类型")
    private RecordType recordType;
    @Comment("第三方账号")
    private ThirdPartyPayAccount thirdPartyPayAccount;
    @Comment("更新时间")
    private Date updateDate;

    @OneToOne
    public Member getMember() {
        return member;
    }

    @OneToOne
    public Account getAccount() {
        return account;
    }

    @Enumerated(STRING)
    public PayType getPayType() {
        return payType;
    }

    @Enumerated(STRING)
    public FlowState getState() {
        return state;
    }

    @Enumerated(STRING)
    public RecordType getRecordType() {
        return recordType;
    }

    @OneToOne(fetch = LAZY)
    public ThirdPartyPayAccount getThirdPartyPayAccount() {
        return thirdPartyPayAccount;
    }
}
