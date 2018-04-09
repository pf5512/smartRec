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
 * Created by 13336 on 2017/2/15.
 */
@Data
@Entity
@Comment("课程收藏")
@Table(name = "sr_course_collect")
public class CourseCollect {
    private Long id;
    @Comment("课程")
    private Course course;
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
    public Course getCourse() {
        return course;
    }

    @OneToOne
    public Member getMember() {
        return member;
    }
}
