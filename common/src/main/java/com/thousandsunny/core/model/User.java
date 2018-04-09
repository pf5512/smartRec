package com.thousandsunny.core.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.AccountEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by Jonathan on 2016/3/27.
 * 用户表
 */
@Data
@Entity
@Table(name = "core_user")
@NoArgsConstructor
public class User extends Human {
    private Long id;

    public User(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Comment("是否管理员")
    private BooleanEnum manager = BooleanEnum.NO;

    @Enumerated(STRING)
    public BooleanEnum getManager() {
        return manager;
    }

    @Comment("账户类型，枚举型")
    private AccountEnum userAccount;

    @Enumerated(STRING)
    public AccountEnum getUserAccount() {
        return userAccount;
    }

    @Comment("所属部门")
    private Org org;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    public Org getOrg() {
        return org;
    }

    @Transient
    public String getHeadImagePath() {
        return headImage == null ? null : headImage.getUrl();
    }

    @Comment("头像")
    private DocumentFile headImage;

    @JsonIgnore
    @OneToOne
    public DocumentFile getHeadImage() {
        return headImage;
    }
}
