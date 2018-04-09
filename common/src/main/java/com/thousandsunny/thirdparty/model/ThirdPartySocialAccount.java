package com.thousandsunny.thirdparty.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.ThirdPartySocialAccountType;
import static javax.persistence.EnumType.STRING;
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
@Comment("第三方账号")
@Table(name = "tp_social_account")
public class ThirdPartySocialAccount {
    private Long id;
    @Comment("第三方账号类型")
    private ThirdPartySocialAccountType accountType;
    @Comment("账号名")
    private String accountName;
    @Comment("标识符")
    private String accountIdentity;
    @Comment("是否激活")
    private BooleanEnum active = YES;
    @Comment("所有人")
    private Member member;

    @Id
    @GeneratedValue(strategy = AUTO)
    @JsonIgnore
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public ThirdPartySocialAccountType getAccountType() {
        return accountType;
    }

    @JsonIgnore
    public String getAccountName() {
        return accountName;
    }

    @Enumerated(STRING)
    public BooleanEnum getActive() {
        return active;
    }

    @Transient
    @JsonIgnore
    public Boolean isAvtive() {
        return active == YES;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }
}
