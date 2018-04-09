package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.core.model.MemberExtInfo;
import com.thousandsunny.service.ModuleKey.EntrepreneursType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.service.ModuleKey.ApplyEnum;
import static com.thousandsunny.service.ModuleKey.ApplyEnum.IN_REVIEW;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在21/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("创业者申请")
@Table(name = "sr_entrepreneurs_apply")
public class EntrepreneursApply {

    private Long id;
    private Member member;
    @Comment("姓名")
    private String name;
    @Comment("手机号")
    private String mobile;
    @Comment("身份证号")
    private String idCardNo;
    private Date applyDate = new Date();
    private Date reviewDate;
    @Comment("身份证正面")
    private CloudFile idCardFront;
    @Comment("半身像")
    private CloudFile half;
    @Comment("申请状态/是否认证")
    private ApplyEnum state = IN_REVIEW;
    @Comment("入伙费")
    private BigDecimal joinMoney;
    @Comment("是否是高级申请")
    private BooleanEnum isAdvanced;
    @Comment("申请类型")
    private EntrepreneursType type;
    @Comment("备注")
    private String notes;
    private MemberExtInfo memberExtInfo;


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
    public CloudFile getIdCardFront() {
        return idCardFront;
    }


    @OneToOne(fetch = LAZY)
    public CloudFile getHalf() {
        return half;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsAdvanced() {
        return isAdvanced;
    }

    @Enumerated(STRING)
    public EntrepreneursType getType() {
        return type;
    }

    @Enumerated(STRING)
    public ApplyEnum getState() {
        return state;
    }

    @OneToOne(fetch = LAZY)
    public MemberExtInfo getMemberExtInfo() {
        return memberExtInfo;
    }
}
