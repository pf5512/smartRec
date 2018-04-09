package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2017/2/13.
 */
@Data
@Entity
@Comment("课程评价")
@Table(name = "sr_course_evaluation")
public class CourseEvaluation {
    private Long id;
    @Comment("评价人")
    private Member member;
    @Comment("课程")
    private Course course;
    @Comment("评价内容")
    private String content;
    @Comment("评价时间")
    private Date createTime = new Date();
    @Comment("评分")
    private Integer score;
    @Comment("图片")
    private List<CloudFile> photos;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;//用于后台删除
    @Comment("是否启用")
    private BooleanEnum isEnable = YES;// 用于后台启用和隐藏

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Course getCourse() {
        return course;
    }

    @OneToMany
    public List<CloudFile> getPhotos() {
        return photos;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete(){
        return isDelete;
    }

    @Transient
    public Boolean getIsDeleteBoolean(){
        return isDelete == YES;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsEnable(){
        return isEnable;
    }

    @Transient
    public Boolean getIsEnableBoolean(){
        return isEnable == YES;
    }

}
