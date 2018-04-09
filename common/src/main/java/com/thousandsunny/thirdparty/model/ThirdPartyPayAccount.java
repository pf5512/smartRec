package com.thousandsunny.thirdparty.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.ThirdPartyPayAccountType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by guitarist on 6/21/16.
 * 青春气贯长虹
 * 勇锐盖过怯弱
 * 进取压倒苟安
 * 年岁有加,并非垂老,理想丢弃,方堕暮年
 */
@Data
@Entity
@Table(name = "tp_pay_account")
public class ThirdPartyPayAccount {
    private Long id;
    @Comment("账户类型")
    private ThirdPartyPayAccountType accountType;
    @Comment("账户名称")
    private String accountName;
    @Comment("账号")
    private String accountNo;
    @Comment("总收益")
    private BigDecimal total;
    @Comment("余额")
    private BigDecimal balance;
    @Comment("可以体现金额")
    private BigDecimal cash;
    @Comment("所有人")
    private Member owner;
    @Comment("是否激活")
    private BooleanEnum active;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public ThirdPartyPayAccountType getAccountType() {
        return accountType;
    }

    @OneToOne(fetch = LAZY)
    public Member getOwner() {
        return owner;
    }

    @Enumerated(STRING)
    public BooleanEnum getActive() {
        return active;
    }
}

