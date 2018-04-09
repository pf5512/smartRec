package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;
import static javax.persistence.TemporalType.DATE;

/**
 * 课程报名时间
 * Created by mu.jie on 2017/2/13.
 */
@Data
@Entity
@Comment("课程报名时间")
@Table(name = "sr_course_sign_up")
public class CourseSignUp {
    private Long id;
    @Comment("时间")
    private Date date;
    @Comment("可报名人数")
    private Integer count;
    @Comment("已报名人数")
    private Integer signedCount;
    @Comment("课程")
    private Course course;
    @Comment("是否删除")
    private BooleanEnum isDelete = BooleanEnum.NO;
    @Comment("是否启用")
    private BooleanEnum isEnable = BooleanEnum.YES;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Course getCourse() {
        return course;
    }

    @Temporal(DATE)
    public Date getDate() {
        return date;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsEnable() {
        return isEnable;
    }

    @Transient
    public Boolean getIsEnableBoolean() {
        return isEnable == BooleanEnum.YES;
    }
}
