package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.*;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("申请(离职退款,离职退款)")
@Table(name = "sr_hp_apply")
public class HpApply {
    private Long id;
    @Comment("岗位")
    private Job job;
    @Comment("申请人")
    private Member applicant;
    @Comment("申请时间")
    private Date date = new Date();
    @Comment("金额")
    private BigDecimal money;
    @Comment("审核状态")
    private ApplyEnum state;
    @Comment("申请类型")
    private ApplyType type;
    @Comment("退款人数")
    private Integer refundCount;
    @Comment("备注")
    private String remark;
    @Comment("离职退款时，退四个月金额的人数")
    private Integer refundFourNum;
    @Comment("离职退款时， 退三个月金额的人数")
    private Integer refundThreeNum;
    @Comment("离职退款时，退三个月减违约金的人数")
    private Integer refundBreachNum;
    @Comment("离职退款时，有违约金的时候违约金的总额")
    private BigDecimal breach;
    @Comment("违约的总天数")
    private Integer breachDays;
    @Comment("违约的岗位招聘数")
    private Integer breachJobApplyNum;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Member getApplicant() {
        return applicant;
    }

    @OneToOne(fetch = LAZY)
    public Job getJob() {
        return job;
    }

    @Enumerated(STRING)
    public ApplyEnum getState() {
        return state;
    }

    @Enumerated(STRING)
    public ApplyType getType() {
        return type;
    }

}
