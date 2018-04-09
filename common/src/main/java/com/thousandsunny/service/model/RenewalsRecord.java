package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.service.ModuleKey;
import com.thousandsunny.service.ModuleKey.RenewType;
import com.thousandsunny.thirdparty.model.AccountFlow;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static java.lang.Boolean.FALSE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

/**
 * 如果这些代码有用，那它们是guitarist在26/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("自动续费")
@Table(name = "sr_renewals_record")
public class RenewalsRecord {

    private Long id;
    @Comment("续费")
    private AutomaticRenewals renewals;//按月悬赏才有，一次性悬赏没有
    @Comment("资金流水")
    private AccountFlow accountFlow;
    @Comment("续费时间")
    private Date date = new Date();
    @Comment("续费状态")
    private RenewType renewType;
    @Comment("实际可分配的金额(车旅费会从这里扣除,导致续费和可分享的费用不相等)")
    private BigDecimal fee;
    @Comment("已被分配")
    private Boolean assigned = FALSE;
    @Comment("第几次续费")
    private Integer times;
    @Comment("岗位")
    private Job job;
    @Comment("续费开始时间")
    private Date startDate;
    @Comment("续费结束时间")
    private Date finalDate;
    @Comment("违约金")
    private BigDecimal breach;
    @Comment("平台处理备注")
    private String remark;
    @Comment("处理违约方式")
    private ModuleKey.RenewalsDealType dealType;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public AutomaticRenewals getRenewals() {
        return renewals;
    }

    @OneToOne
    public AccountFlow getAccountFlow() {
        return accountFlow;
    }

    @Enumerated(STRING)
    public ModuleKey.RenewalsDealType getDealType(){
        return dealType;
    }

    @Enumerated(STRING)
    public RenewType getRenewType() {
        return renewType;
    }

    @OneToOne
    public Job getJob() {
        return job;
    }
}
