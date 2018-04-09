package com.thousandsunny.service.model;

/**
 * Created by Xiaoxuewei on 2016/11/30.
 */

import com.thousandsunny.common.entity.Comment;
import lombok.Data;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
import static com.thousandsunny.core.ModuleKey.BooleanEnum;
import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;

@Data
@Entity
@Comment("线下付款银行")
@Table(name="sr_line_payment_bank")
public class LinePaymentBank {

    private Long id;
    @Comment("开户行名称")
    private String bankName;
    @Comment("银行账号")
    private String bankNo;
    @Comment("开户名称")
    private  String payee;
    @Comment("删除标志")
    private BooleanEnum isDelete = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @Enumerated(STRING)
    public BooleanEnum getIsDelete() {
        return isDelete;
    }
}
