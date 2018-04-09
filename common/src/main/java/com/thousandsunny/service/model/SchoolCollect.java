package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by 13336 on 2017/2/14.
 */
@Data
@Entity
@Comment("学校收藏")
@Table(name = "sr_school_collect")
public class SchoolCollect {
    private Long id;
    @Comment("学校")
    private School school;
    @Comment("收藏者")
    private Member member;
    @Comment("收藏时间")
    private Date date = new Date();
    @Comment("删除标记")
    private ModuleKey.BooleanEnum collectEver = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public ModuleKey.BooleanEnum getCollectEver() {
        return collectEver;
    }

    @OneToOne
    public School getSchool() {
        return school;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }
}
