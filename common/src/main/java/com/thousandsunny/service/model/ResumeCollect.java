package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("简历收藏")
@Table(name = "sr_resume_collect")
public class ResumeCollect {


    private Long id;
    @Comment("简历")
    private Resume resume;
    @Comment("收藏者")
    private Member member;
    @Comment("收藏时间")
    private Date date;
    @Comment("删除标记")
    private BooleanEnum collectEver = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public BooleanEnum getCollectEver() {
        return collectEver;
    }

    @OneToOne
    public Resume getResume() {
        return resume;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }
}
