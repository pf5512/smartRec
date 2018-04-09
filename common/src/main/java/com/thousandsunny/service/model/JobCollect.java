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
@Comment("岗位收藏")
@Table(name = "sr_job_collect")
public class JobCollect {
    private Long id;
    @Comment("岗位")
    private Job job;
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
    public Job getJob() {
        return job;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }
}
