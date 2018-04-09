package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static com.thousandsunny.service.ModuleKey.BenefitItemType;
import static com.thousandsunny.service.ModuleKey.BenefitItemState;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在24/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("好处，权益")
@Table(name = "sr_benefit_item")
public class BenefitItem {

    private Long id;
    @Comment("logo")
    private CloudFile logo;
    @Comment("标题")
    private String title;
    @Comment("标题")
    private String linkTitle;
    @Comment("标题")
    private String linkUrl;
    @Comment("描述")
    private String description;
    @Comment("好处类型")
    private BenefitItemType type;
    @Comment("是否还可用")
    private BooleanEnum valid;
    @Comment("创建时间")
    private Date createDate = new Date();
    @Comment("开始有效时间")
    private Date effectiveDate;
    @Comment("最后有效时间")
    private Date invalidDate;
    @Comment("好处")
    private Benefit benefit;
    @Comment("我的好处")
    private BenefitRel benefitRel;
    @Comment("好处状态")
    private BenefitItemState state;
    @Comment("备注")
    private String remark;


    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Column(columnDefinition = TEXT)
    public String getDescription() {
        return description;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getLogo() {
        return logo;
    }

    @Enumerated(STRING)
    public BenefitItemType getType() {
        return type;
    }

    @Enumerated(STRING)
    public BooleanEnum getValid() {
        return valid;
    }

    @OneToOne(fetch = LAZY)
    public Benefit getBenefit() {
        return benefit;
    }

    @ManyToOne(fetch = LAZY)
    public BenefitRel getBenefitRel() {
        return benefitRel;
    }

    @Enumerated(STRING)
    public BenefitItemState getState() {
        return state;
    }
}
