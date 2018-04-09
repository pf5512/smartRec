package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在12/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("会员聊天配置")
@Table(name = "sr_member_chat_profile")
public class MemberChatProfile {

    private Long id;
    @Comment("发起人")
    private Member owner;
    @Comment("聊天对象")
    private Member chatUser;
    @Comment("是否置顶")
    private BooleanEnum isTop;
    @Comment("是否免打扰")
    private BooleanEnum isNoDisturb;
    private Date lastChat;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getOwner() {
        return owner;
    }

    @OneToOne
    public Member getChatUser() {
        return chatUser;
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
    public Boolean getIsTopBool() {
        return isTop==YES;
    }

    @Transient
    public Boolean getIsNoDisturbBool() {
        return isNoDisturb==YES;
    }
}
