package com.thousandsunny.cms.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2017/3/9.
 */
@Data
@Comment("用户角色表")
@Entity
@Table(name = "cms_member_role")
public class MemberRole {
    private Long id;
    @Comment("用户")
    private Member member;
    @Comment("角色")
    private Role role;
    @Comment("删除标记")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Role getRole() {
        return role;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Transient
    public Boolean getIsDeleteBoolean() {
        return isDelete == YES;
    }

}
