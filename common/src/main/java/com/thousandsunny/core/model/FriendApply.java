package com.thousandsunny.core.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.ApplyState;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * 好友请求
 */
@Data
@Entity
@Comment("好友申请")
@Table(name = "core_friend_apply")
public class FriendApply {
    private Long id;

    @Comment("申请日期")
    private Date applyDate;
    @Comment("申请人")
    private Member applicant;
    @Comment("添加的好友")
    private Member approver;
    @Comment("申请状态")
    private ApplyState applyState;
    @Comment("是否已读")
    private BooleanEnum isRead;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Member getApplicant() {
        return applicant;
    }

    @OneToOne
    public Member getApprover() {
        return approver;
    }

    @Enumerated(STRING)
    public ApplyState getApplyState() {
        return applyState;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsRead() {
        return isRead;
    }

}
