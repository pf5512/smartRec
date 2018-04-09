package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.AccountEnum;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;

import java.util.Objects;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.core.ModuleKey.IdentityType;
import static com.thousandsunny.core.ModuleKey.IdentityType.NONE;
import static java.util.Objects.isNull;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("会员")
@Table(name = "core_member")
public class Member extends BaseMember {
    private Long id;
    @Comment("慧聘账号")
    private String hpAccount;
    @Comment("创业者身份")
    private IdentityType entrepreneurLevel = NONE;
    @Comment("合伙人身份")
    private BooleanEnum partnerLevel = NO;
    @Comment("身份信息是否审核通过")
    private BooleanEnum identityHasPass = NO;
    @Comment("角色")
    private AccountEnum role;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return this.id;
    }

    @Enumerated(STRING)
    public IdentityType getEntrepreneurLevel() {
        return entrepreneurLevel;
    }

    @Enumerated(STRING)
    public BooleanEnum getIdentityHasPass() {
        return identityHasPass;
    }

    @Enumerated(STRING)
    public BooleanEnum getPartnerLevel() {
        return partnerLevel;
    }


    @Transient
    public Boolean getHasPassed() {
        return identityHasPass == YES;
    }

    @Enumerated(STRING)
    public AccountEnum getRole() {
        return role;
    }

    @Transient
    public String getRoleTitle() {
        return isNull(role) ? null : role.getTitle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;
        if (!super.equals(o)) return false;
        Member member = (Member) o;
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
