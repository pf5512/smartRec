package com.thousandsunny.thirdparty.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.service.ModuleKey.KeyPercentage;
import com.thousandsunny.service.model.*;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.thirdparty.ModuleKey.*;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Table(name = "tp_account_flow")
public class AccountFlow {
    private Long id;

    @Comment("账户")
    private Account account;

    @Comment("提现账户")
    private WithdrawAccount withdrawAccount;

    @Comment("金额")
    private BigDecimal amount;

    @Comment("金额方向类型")
    private ChargeType type;

    @Comment("记录类型")
    private RecordType recordType;

    @Comment("来源")
    private SourceType source;

    @Comment("支付类型")
    private PayType payType;

    @Comment("线下支付类型")
    private PayOfflineType payOfflineType;

    @Comment("创建时间")
    private Date createDate = new Date();

    @Comment("流水状态")
    private FlowState state;

    @Comment("订单号")
    private String orderNo;

    @Comment("更新时间")
    private Date updateDate = new Date();

    @Comment("备注(文字信息)")
    private String remark;

    @Comment("备注(后台编辑后的离线支付的备注信息)")
    private String remarks;

    @Comment("提现/退费 申请时间(只有当记录类型为提现/退费时有效)")
    private Date applyDate = new Date();

    @Comment("提现/退费 到账时间(只有当记录类型为提现/退费时有效)")
    private Date receivedDate = new Date();

    @Comment("提现/退费 失败原因")
    private String reason;

    @Comment("创业者")
    private EntrepreneursApply entrepreneursApply;

    @Comment("合伙人")
    private PartnerApply partnerApply;

    @Comment("推荐记录")
    private JobApplyRecord jobApplyRecord;

    @Comment("课程申请")
    private CourseApply courseApply;

    @Comment("退款申请")
    private CourseRefundApply courseRefundApply;

    @Comment("付款第三方账户")
    private ThirdPartyPayAccount thirdPartyPayAccount;

    @Comment("工作")
    private Job job;
    @Comment("岗位退款申请")
    private HpApply hpApply;

    @Comment("岗位收益关系")
    private KeyPercentage relation;//在定时器中分赃逻辑中设置,保存收益关系，在后台岗位奖励明细中关系中显示


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    @JsonIgnore
    public Account getAccount() {
        return account;
    }

    @OneToOne
    public WithdrawAccount getWithdrawAccount() {
        return withdrawAccount;
    }

    @Enumerated(STRING)
    public ChargeType getType() {
        return type;
    }

    @Enumerated(STRING)
    public RecordType getRecordType() {
        return recordType;
    }

    @Enumerated(STRING)
    public FlowState getState() {
        return state;
    }

    @Enumerated(STRING)
    public SourceType getSource() {
        return source;
    }

    @Enumerated(STRING)
    public PayType getPayType() {
        return payType;
    }

    @Enumerated(STRING)
    public PayOfflineType getPayOfflineType() {
        return payOfflineType;
    }

    @OneToOne(fetch = LAZY)
    public Job getJob() {
        return job;
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
    public PartnerApply getPartnerApply() {
        return partnerApply;
    }

    @OneToOne(fetch = LAZY)
    public ThirdPartyPayAccount getThirdPartyPayAccount() {
        return thirdPartyPayAccount;
    }

    @OneToOne(fetch = LAZY)
    public CourseApply getCourseApply() {
        return courseApply;
    }

    @OneToOne(fetch = LAZY)
    public CourseRefundApply getCourseRefundApply() {
        return courseRefundApply;
    }

    @OneToOne(fetch = LAZY)
    public HpApply getHpApply() {
        return hpApply;
    }

    @Enumerated(STRING)
    public KeyPercentage getRelation(){
        return relation;
    }

}
