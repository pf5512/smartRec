package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.CourseApplyState;
import com.thousandsunny.thirdparty.ModuleKey.PayType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.common.RandomNumberUtil.randomUUIDString;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在11/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("课程报名/订单")
@Table(name = "sr_course_apply")
public class CourseApply {

    private Long id;
    @Comment("序列号")
    private String serialNo = randomUUIDString();
    @Comment("申请人")
    private Member member;
    @Comment("学校")
    private School school;
    @Comment("价格")
    private BigDecimal price;
    @Comment("红包")
    private BigDecimal discount;
    @Comment("课程")
    private Course course;
    @Comment("报名时间")
    private Date date = new Date();
    @Comment("培训时间")
    private Date trainDate;
    @Comment("申请状态")
    private CourseApplyState state;
    @Comment("是否免费培训")
    private BooleanEnum isUseFee;
    @Comment("免费培训")
    private BenefitItem benefitItem;
    @Comment("付款方式")
    private PayType payType;
    @Comment("使用红包")
    private List<RedPacketReceive> redPacketReceives;
    @Comment("备注")
    private String remark;
    @Comment("订单关闭时间")
    private Date closeDate;
    @Comment("支付时间")
    private Date payDate;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public School getSchool() {
        return school;
    }

    @OneToOne(fetch = LAZY)
    public Course getCourse() {
        return course;
    }

    @OneToOne
    public BenefitItem getBenefitItem() {
        return benefitItem;
    }

    @Enumerated(STRING)
    public CourseApplyState getState() {

        return state;
    }

    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }


    @Enumerated(STRING)
    public BooleanEnum getIsUseFee() {
        return isUseFee;
    }

    @OneToMany
    public List<RedPacketReceive> getRedPacketReceives() {
        return redPacketReceives;
    }
}
