package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;

import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("屏蔽查看简历的用户表")
@Table(name = "sr_resume_blocked")
public class ResumeBlocked {

    private Long id;
    @Comment("屏蔽者")
    private Member member;

    @Comment("被屏蔽的用户")
    private Member resumeMember;

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
    public Member getResumeMember() {
        return resumeMember;
    }


}
