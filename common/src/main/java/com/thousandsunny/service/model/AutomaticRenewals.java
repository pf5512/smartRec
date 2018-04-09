package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.thirdparty.model.Account;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;
import static javax.persistence.TemporalType.DATE;

/**
 * 如果这些代码有用，那它们是guitarist在26/10/2016写的;
 * 如果没用，那我就不知道是谁写的了。
 */
@Data
@Entity
@Comment("自动续费设置")
@Table(name = "sr_automatic_renewals")
public class AutomaticRenewals {

    private Long id;
    @Comment("岗位")
    private Job job;
    @Comment("员工")
    private Member worker;
    @Comment("是否自动续费")
    private Boolean auto;
    @Comment("入职时间")
    private Date avaDate;
    @Comment("开始时间")
    private Date startDate;
    @Comment("最晚时间")
    private Date finalDate;
    @Comment("下次续费的时间(yyyy-MM-dd)")
    private Date nextTime;
    @Comment("扣费的账户")
    private Account account;
    @Comment("费用")
    private BigDecimal fee;
    @Comment("违约金")
    private BigDecimal breach = new BigDecimal(0);
    @Comment("第几次续费")
    private Integer times = 1;
    @Comment("是否删除")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToOne
    public Job getJob() {
        return job;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }

    @Temporal(DATE)
    public Date getNextTime() {
        return nextTime;
    }

    @Temporal(DATE)
    public Date getAvaDate() {
        return avaDate;
    }

    @Temporal(DATE)
    public Date getStartDate() {
        return startDate;
    }

    @Temporal(DATE)
    public Date getFinalDate() {
        return finalDate;
    }

    @OneToOne
    public Account getAccount() {
        return account;
    }

    @OneToOne
    public Member getWorker() {
        return worker;
    }

}
