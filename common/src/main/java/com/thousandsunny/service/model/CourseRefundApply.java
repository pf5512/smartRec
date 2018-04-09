package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.CourseRefundWay;
import com.thousandsunny.service.ModuleKey.CourseRefundReason;
import com.thousandsunny.service.ModuleKey.CourseApplyState;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * 课程退款申请
 * Created by mu.jie on 2017/2/13.
 */
@Data
@Entity
@Comment("课程退款申请")
@Table(name = "sr_course_refund_apply")
public class CourseRefundApply {
    private Long id;
    @Comment("申请人")
    private Member member;
    @Comment("订单")
    private CourseApply courseApply;
    @Comment("退款时间")
    private Date createTime = new Date();
    @Comment("取消退款时间")
    private Date cancelTime;
    @Comment("退款原因")
    private CourseRefundReason reason;
    @Comment("退款备注")
    private String remark;
    @Comment("退款总金额")
    private BigDecimal amount;
    @Comment("平台退款金额")
    private BigDecimal platform;
    @Comment("学校退款金额")
    private BigDecimal school;
    @Comment("状态")
    private CourseApplyState state;//COURSE_ORDER_REFUNDING("退款中"), COURSE_ORDER_REFUNDED("已退款"), COURSE_ORDER_REFUND_FAIL("退款失败")
    @Comment("审核原因")
    private String refundRemark;
    @Comment("审核时间")
    private Date auditTime;
    @Comment("退款到账时间")
    private Date receivedDate;
    @Comment("退款途径")
    private CourseRefundWay way;

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
    public CourseApply getCourseApply() {
        return courseApply;
    }

    @Enumerated(EnumType.STRING)
    public CourseApplyState getState() {
        return state;
    }

    @Enumerated(EnumType.STRING)
    public CourseRefundReason getReason() {
        return reason;
    }

    @Enumerated(EnumType.STRING)
    public CourseRefundWay getWay(){
        return way;
    }
}
