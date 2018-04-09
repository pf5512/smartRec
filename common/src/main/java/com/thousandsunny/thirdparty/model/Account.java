package com.thousandsunny.thirdparty.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.common.RandomNumberUtil.randomUUIDString;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("会员账户")
@Table(name = "tp_account")
public class Account {
    private Long id;
    private String uid = randomUUIDString();
    @Comment("总金额")
    private BigDecimal total = new BigDecimal(0);
    @Comment("余额")
    private BigDecimal balance = new BigDecimal(0);
    @Comment("冻结金额")
    private BigDecimal freezingAmount = new BigDecimal(0);
    @Comment("会员")
    private Member member;
    @Comment("创建日期")
    private Date createDate = new Date();
    @Comment("支付密码")
    private String payPassword;
    private BooleanEnum valid = NO;
    @Comment("是否是系统账户")
    private BooleanEnum zues = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @Enumerated(STRING)
    public BooleanEnum getValid() {
        return valid;
    }

    @Enumerated(STRING)
    public BooleanEnum getZues() {
        return zues;
    }
}
