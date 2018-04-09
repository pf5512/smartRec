package com.thousandsunny.thirdparty.model;


import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static javax.persistence.GenerationType.AUTO;

@Data
@Entity
@Comment("账户申请记录表")
@Table(name = "tp_account_apply_record")
public class TpAccountApplyRecord extends BaseAccountApplyRecord {
    private Long id;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }
}
