package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2017/2/21.
 */
@Data
@Entity
@Comment("订单生成器")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sr_course_apply_gen")
public class CourseApplyGen {
    private Long id;
    private Long seq = 1L;
    private Date date = new Date();
    private CourseApply courseApply;

    public CourseApplyGen(Long seq){
        this.seq = seq;
    }
    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public CourseApply getCourseApply() {
        return courseApply;
    }
}
