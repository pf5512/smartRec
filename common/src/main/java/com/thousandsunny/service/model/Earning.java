package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.service.ModuleKey.EarningType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by admin on 2016/11/2.
 */
@Data
@Entity
@Comment("收益")
@Table(name = "sr_earning")
public class Earning {
    private Long id;
    @Comment("创业者")
    private Entrepreneurs entrepreneurs;
    @Comment("合伙人")
    private Partner partner;
    @Comment("收益")
    private BigDecimal amount;
    @Comment("收益类型")
    private EarningType earningType;
    @Comment("日期")
    private Date date = new Date();
    @Comment("工作申请表")
    private JobApplyRecord jobApplyRecord;
    @Comment("被创业者推荐成为创业者的创业者")
    private Entrepreneurs beEntrepreneurs;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Entrepreneurs getEntrepreneurs() {
        return entrepreneurs;
    }

    @OneToOne(fetch = LAZY)
    public Partner getPartner() {
        return partner;
    }

    @OneToOne(fetch = LAZY)
    public JobApplyRecord getJobApplyRecord() {
        return jobApplyRecord;
    }

    @Enumerated(STRING)
    public EarningType getEarningType() {
        return earningType;
    }

    @OneToOne(fetch = LAZY)
    public Entrepreneurs getBeEntrepreneurs() {
        return beEntrepreneurs;
    }


}
