package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.CloudFile;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.YES;
import static com.thousandsunny.service.ModuleKey.RecruitmentState;
import static com.thousandsunny.service.ModuleKey.RecruitmentType;
import static com.thousandsunny.thirdparty.ModuleKey.PayType;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在20/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("岗位")
@Table(name = "sr_job")
public class Job {
    private Long id;
    @Comment("工作名称")
    private String name;
    @Comment("店铺")
    private Shop shop;
    @Comment("薪资待遇")
    private JobConstant salary;
    @Comment("工作年限")
    private JobConstant period;
    @Comment("发布时间")
    private Date date = new Date();
    @Comment("工作类型")
    private JobType jobType;
    private CloudFile logo;
    @Comment("招聘类型")
    private RecruitmentType recType;
    @Comment("刷新时间")
    private Date fresh;
    @Comment("刷新次数")
    private Integer freshCount = 0;
    @Comment("奖励金额")
    private BigDecimal reward;
    @Comment("工作描述")
    private String description;
    @Comment("自动续费")
    private Boolean isAuto = false;
    @Comment("招聘状态")
    private RecruitmentState state;
    @Comment("招聘(未招)人数")
    private Integer epmCount;
    @Comment("在职人数")
    private Integer workerCount = 0;
    @Comment("离职人数")
    private Integer quitterCount = 0;
    @Comment("退费人数")
    private Integer refundCount = 0;
    @Comment("支付方式")
    private PayType payType;
    private BooleanEnum isDelete = NO;
    @Comment("是否启用")
    private BooleanEnum isEnable = YES;
    @Comment("需要改变的人数,增加招聘人数用")
    private Integer changeCount;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne(fetch = LAZY)
    public Shop getShop() {
        return shop;
    }

    @OneToOne(fetch = LAZY)
    public JobType getJobType() {
        return jobType;
    }

    @OneToOne(fetch = LAZY)
    public CloudFile getLogo() {
        return logo;
    }

    @OneToOne(fetch = LAZY)
    public JobConstant getSalary() {
        return salary;
    }

    @OneToOne(fetch = LAZY)
    public JobConstant getPeriod() {
        return period;
    }

    @Enumerated(STRING)
    public RecruitmentType getRecType() {
        return recType;
    }

    @Enumerated(STRING)
    public RecruitmentState getState() {
        return state;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Enumerated(STRING)
    public PayType getPayType() {
        return payType;
    }

    @Transient
    public Long getStoreId() {
        if (shop != null)
            return shop.getId();
        else return null;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsEnable() {
        return isEnable;
    }

    @Transient
    public Boolean getIsEnableBoolean() {
        return isEnable == YES;
    }

}
