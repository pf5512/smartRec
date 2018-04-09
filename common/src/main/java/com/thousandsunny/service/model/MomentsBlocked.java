package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by admin on 2016/10/25.
 */


@Data
@Comment("屏蔽查看动态的用户表")
@Entity
@Table(name = "sr_moments_blocked")
public class MomentsBlocked {
    private Long id;
    @Comment("屏蔽者")
    private Member member;

    @Comment("屏蔽查看动态的用户")
    private Member momentsMember;
    @Comment("屏蔽时间")
    private Date date = new Date();


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
    public Member getMomentsMember() {
        return momentsMember;
    }
}
