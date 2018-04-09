package com.thousandsunny.thirdparty.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.NO;
import static javax.persistence.GenerationType.AUTO;

/**
 * 账户冻结记录表
 * Created by mu.jie on 2017/2/27.
 */
@Data
@Entity
@Comment("账户冻结记录")
@Table(name = "tp_account_freeze_record")
public class AccountFreezingRecord {
    private Long id;
    @Comment("账户")
    private Account account;
    @Comment("冻结时间")
    private Date date = new Date();
    @Comment("解冻时间")
    private Date unfreezeDate;
    @Comment("冻结金额")
    private BigDecimal amount = new BigDecimal(0);
    @Comment("是否解冻")
    private BooleanEnum isUnfreeze = NO;
    @Comment("备注")
    private String remark;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @ManyToOne
    public Account getAccount(){
        return account;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsUnfreeze(){
        return isUnfreeze;
    }
}
