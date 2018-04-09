package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在9/11/16写的;
 * 如果没用，那我就不知道是谁写的了。
 * <p>
 * 会员-群聊
 */
@Data
@Entity
@Comment("会员群聊关系表")
@Table(name = "core_member_group")
@NoArgsConstructor
public class MemberGroup {

    private Long id;
    @Comment("会员")
    private Member member;
    @Comment("群聊")
    private Group group;
    @Comment("是否置顶")
    private BooleanEnum isTop = NO;
    @Comment("是否勿扰")
    private BooleanEnum isNoDisturb = NO;
    @Comment("是否是群主")
    private BooleanEnum isOwner = NO;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    public MemberGroup(Group group, Member member) {
        this.group = group;
        this.member = member;
    }

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @OneToOne
    public Group getGroup() {
        return group;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsTop() {
        return isTop;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsNoDisturb() {
        return isNoDisturb;
    }

    @Transient
    public Boolean getIsNoDisturbBool() {
        return isNoDisturb == YES;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsOwner() {
        return isOwner;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Transient
    public Boolean getIsOwnerBoolean() {
        return isOwner == YES;
    }

    @Transient
    public Boolean getIsTopBoolean() {
        return isTop == YES;
    }

    @Transient
    public Boolean getIsNoDisturbBoolean() {
        return isNoDisturb == YES;
    }


}
