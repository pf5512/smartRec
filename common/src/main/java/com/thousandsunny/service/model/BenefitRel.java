package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.model.AccountFlow;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.service.ModuleKey.BenefitType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static javax.persistence.TemporalType.DATE;

/**
 * 如果这些代码有用，那它们是guitarist在24/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("我的好处")
@Table(name = "sr_benefit_rel")
public class BenefitRel {

    private Long id;
    private Member member;
    @Comment("好处")
    private Benefit benefit;
    @Comment("日期")
    private Date date;
    @Comment("有效")
    private BooleanEnum valid;
    @Comment("类型")
    private BenefitType type;
    @Comment("流水")
    private AccountFlow flow;
    @Comment("开始有效时间")
    private Date effectiveDate;
    @Comment("最后有效时间")
    private Date invalidDate;
    @Comment("来源的工作")
    private Job job;
    @Comment("好处具项")
    private List<BenefitItem> benefitItem;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
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

    @OneToOne(fetch = LAZY)
    public AccountFlow getFlow() {
        return flow;
    }

    @OneToOne
    public Job getJob() {
        return job;
    }

    @Temporal(DATE)
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    @Temporal(DATE)
    public Date getInvalidDate() {
        return invalidDate;
    }

    @OneToMany(mappedBy = "benefitRel")
    public List<BenefitItem> getBenefitItem() {
        return benefitItem;
    }
}
