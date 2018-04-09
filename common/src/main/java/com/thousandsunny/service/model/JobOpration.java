package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.thirdparty.model.Account;
import com.thousandsunny.thirdparty.model.AccountFlow;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by admin on 2016/11/29.
 */

@Data
@Entity
@Comment("岗位操作")
@Table(name = "sr_job_opration")
public class JobOpration {
    private Long id;
    @Comment("操作时间")
    private Date date = new Date();
    @Comment("流水")
    private AccountFlow accountFlow;
    @Comment("操作的人数")
    private Integer memberCount = 0;
    @Comment("岗位")
    private Job job;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }


    @OneToOne(fetch = FetchType.LAZY)
    public AccountFlow getAccountFlow() {
        return accountFlow;
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Job getJob() {
        return job;
    }


}
