package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.service.ModuleKey.BenefitApplyState;
import static com.thousandsunny.service.ModuleKey.BenefitType;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在24/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("我的好处_申请")
@Table(name = "sr_benefit_apply")
public class BenefitApply {

    private Long id;
    @Comment("申请人")
    private Member member;
    @Comment("好处")
    private Benefit benefit;
    @Comment("创建日期")
    private Date date = new Date();
    @Comment("有效")
    private BooleanEnum valid;
    @Comment("类型")
    private BenefitType type;
    @Comment("状态")
    private BenefitApplyState applyState;
    @Comment("今天是否提醒,一天一次")
    private BooleanEnum isTodayRemind;
    @Comment("提醒时间")
    private List<BenefitApplyDate> remindDates;
    @Comment("申请人填写姓名")
    private String name;
    @Comment("申请人填写电话")
    private String phoneNumber;
    @Comment("原因")
    private String reason;
    @Comment("申请人填写店名")
    private String storeName;
    @Comment("申请人上传照片")
    private List<CloudFile> pics;
    @Comment("申请人填写金额")
    private BigDecimal amount;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;
    @Comment("备注")
    private String remark;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }


    @OneToOne(fetch = LAZY)
    public Member getMember() {
        return member;
    }

    @OneToOne(fetch = LAZY)
    public Benefit getBenefit() {
        return benefit;
    }

    @Enumerated(STRING)
    public BooleanEnum getValid() {
        return valid;
    }

    @Enumerated(STRING)
    public BenefitType getType() {
        return type;
    }

    @Enumerated(STRING)
    public BenefitApplyState getApplyState() {
        return applyState;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsTodayRemind() {
        return isTodayRemind;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete(){
        return isDelete;
    }

    @OneToMany(cascade = ALL)
    public List<CloudFile> getPics() {
        return pics;
    }

    @OneToMany
    public List<BenefitApplyDate> getRemindDates(){
        return remindDates;
    }
}
