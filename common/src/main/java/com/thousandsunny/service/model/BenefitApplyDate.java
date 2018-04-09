package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2016/12/14.
 */
@Data
@Entity
@Comment("我的好处_申请提醒时间")
@Table(name = "sr_benefit_apply_date")
public class BenefitApplyDate {
    private Long id;
    private Date date = new Date();

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }
}
