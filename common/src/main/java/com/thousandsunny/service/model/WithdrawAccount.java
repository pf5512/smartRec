package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.WithdrawType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("提现账户")
@Table(name = "sr_withdraw_account")
public class WithdrawAccount {
    private Long id;
    @Comment("用户")
    private Member member;
    @Comment("学校")
    private School school;
    @Comment("提现类型")
    private WithdrawType type;
    @Comment("用户名")
    private String name;
    @Comment("账号")
    private String account;
    @Comment("开户银行")
    private String bank;
    @Comment("身份证号码")
    private String idCardNo;
    @Comment("支行")
    private String branchBank;
    @Comment("创建时间")
    private Date date = new Date();
    @Comment("身份证正面照")
    private CloudFile idCard;
    @Comment("半身像")
    private CloudFile half;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = LAZY)
    public School getSchool(){
        return school;
    }

    @Enumerated(STRING)
    public WithdrawType getType() {
        return type;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getIdCard() {
        return idCard;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getHalf() {
        return half;
    }
}
