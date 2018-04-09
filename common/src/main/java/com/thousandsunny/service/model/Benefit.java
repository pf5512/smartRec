package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.TEXT;
import static com.thousandsunny.service.ModuleKey.BenefitType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在24/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("好处")
@Table(name = "sr_benefit")
public class Benefit {

    private Long id;
    @Comment("logo")
    private CloudFile logo;
    @Comment("标题")
    private String title;
    @Comment("简介")
    private String summary;
    @Comment("描述")
    private String description;
    @Comment("好处类型")
    private BenefitType type;
    @Comment("是否还可用")
    private BooleanEnum valid;

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
    public BenefitType getType() {
        return type;
    }

    @Enumerated(STRING)
    public BooleanEnum getValid() {
        return valid;
    }
}
