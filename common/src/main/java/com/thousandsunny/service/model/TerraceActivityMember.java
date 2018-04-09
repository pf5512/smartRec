package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;

/**
 * Created by Xiaoxuewei on 2016/11/29.
 */
@Data
@Entity
@Comment("平台活动是否已读")
@Table(name = "sr_terrace_activity_member")
public class TerraceActivityMember {
    private Long id;
    @Comment("平台活动")
    private TerraceActivity terraceActivity;
    @Comment("会员")
    private Member member;
    @Comment("是否已读")
    private BooleanEnum isRead = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public TerraceActivity getTerraceActivity() {
        return terraceActivity;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsRead() {
        return isRead;
    }
}
