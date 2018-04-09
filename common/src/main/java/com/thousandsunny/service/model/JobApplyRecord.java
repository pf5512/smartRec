package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.service.ModuleKey.*;
import static com.thousandsunny.service.ModuleKey.RecState.NOT_WORK;
import static com.thousandsunny.service.ModuleKey.RefundEnum.HAS_NOT_REFUNDED;
import static com.thousandsunny.service.ModuleKey.RewardEnum.HAS_NOT_REWARD;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在21/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("工作申请表")
@Table(name = "sr_job_apply_record")
public class JobApplyRecord {

    private Long id;
    @Comment("推荐人")
    private Member referral;
    @Comment("职位")
    private Job job;
    @Comment("被推荐人")
    private Member receiver;
    @Comment("店铺")
    private Shop shop;
    @Comment("推荐时间")
    private Date date = new Date();
    @Comment("推荐状态")
    private RecState recState = NOT_WORK;
    @Comment("上班时间")
    private Date startDate;
    @Comment("离职时间")
    private Date resignDate;
    @Comment("离职说明")
    private String resignRemark;
    @Comment("离职类型")
    private ResignEnum resignType;
    @Comment("退款状态")
    private RefundEnum refund = HAS_NOT_REFUNDED;
    @Comment("奖励状态")
    private RewardEnum reward = HAS_NOT_REWARD;
    @Comment("退款申请---在岗位发起退款时将退款申请保存进来")
    private HpApply hpApply;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getReferral() {
        return referral;
    }

    @OneToOne(fetch = LAZY)
    public Job getJob() {
        return job;
    }

    @OneToOne
    public Member getReceiver() {
        return receiver;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @Enumerated(STRING)
    public RecState getRecState() {
        return recState;
    }

    @Enumerated(STRING)
    public ResignEnum getResignType() {
        return resignType;
    }

    @Enumerated(STRING)
    public RefundEnum getRefund() {
        return refund;
    }

    @Enumerated(STRING)
    public RewardEnum getReward() {
        return reward;
    }

    @OneToOne(fetch = LAZY)
    public HpApply getHpApply() {
        return hpApply;
    }
}
