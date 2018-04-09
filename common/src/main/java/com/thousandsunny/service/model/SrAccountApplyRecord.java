package com.thousandsunny.service.model;


import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.thirdparty.model.BaseAccountApplyRecord;
import lombok.Data;

import javax.persistence.*;

import static com.thousandsunny.service.ModuleKey.SrAccountApplyRecordType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("账户申请记录表")
@Table(name = "tp_account_apply_record")
public class SrAccountApplyRecord extends BaseAccountApplyRecord {
    private Long id;
    @Comment("来源")
    private SrAccountApplyRecordType source;
    @Comment("创业者")
    private EntrepreneursApply entrepreneursApply;
    @Comment("合伙人")
    private PartnerApply partnerApply;
    @Comment("推荐记录")
    private JobApplyRecord jobApplyRecord;
    @Comment("推荐记录")
    private Job job;
    @Comment("课程报名")
    private CourseApply courseApply;
    @Comment("课程报名退款")
    private CourseRefundApply courseRefundApply;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public SrAccountApplyRecordType getSource() {
        return source;
    }


    @OneToOne
    public JobApplyRecord getJobApplyRecord() {
        return jobApplyRecord;
    }

    @OneToOne(fetch = LAZY)
    public EntrepreneursApply getEntrepreneursApply() {
        return entrepreneursApply;
    }

    @OneToOne(fetch = LAZY)
    public Job getJob() {
        return job;
    }

    @OneToOne(fetch = LAZY)
    public PartnerApply getPartnerApply() {
        return partnerApply;
    }

    @OneToOne(fetch = LAZY)
    public CourseApply getCourseApply(){
        return courseApply;
    }

    @OneToOne(fetch = LAZY)
    public CourseRefundApply getCourseRefundApply(){
        return courseRefundApply;
    }
}
