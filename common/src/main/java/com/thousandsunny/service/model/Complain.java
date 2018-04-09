package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.ComplainType;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.TEXT;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("投诉")
@Table(name = "sr_complain")
public class Complain {
    private Long id;
    @Comment("投诉人")
    private Member complainant;
    @Comment("被投诉人")
    private Member defendant;
    @Comment("被投诉店铺")
    private Shop shop;
    @Comment("被投诉学校")
    private School school;
    @Comment("投诉类型")
    private ComplainType type;
    @Comment("投诉原因")
    private String reason;
    @Comment("申诉时间")
    private Date date = new Date();
    @Comment("是否处理")
    private BooleanEnum isDeal = NO;
    @Comment("处理意见备注")
    private String opinion;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return this.id;
    }

    @OneToOne(fetch = LAZY)
    public Member getComplainant() {
        return complainant;
    }

    @OneToOne(fetch = LAZY)
    public Member getDefendant() {
        return defendant;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @OneToOne(fetch = LAZY)
    public School getSchool() {
        return school;
    }


    @Enumerated(STRING)
    public ComplainType getType() {
        return type;
    }

    @Column(columnDefinition = TEXT)
    public String getReason() {
        return reason;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDeal() {
        return isDeal;
    }
}
