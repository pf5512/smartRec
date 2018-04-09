package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.RedPacketCategory;
import com.thousandsunny.service.ModuleKey.RedPacketSendType;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2017/2/15.
 */
@Data
@Entity
@Comment("红包")
@Table(name = "sr_red_packet")
public class RedPacket {
    private Long id;
    @Comment("红包金额")
    private BigDecimal amount;
    @Comment("红包有效期")
    private Date validDate;
    @Comment("发布时间")
    private Date createDate = new Date();
    @Comment("红包发放的用户")
    private List<Member> members;
    @Comment("红包类别")
    private RedPacketCategory category;
    @Comment("红包发放对象")
    private RedPacketSendType sendType;
    @Comment("特定用户类型")
    private String specialTypes;//将枚举RedPacketSpecialType的num,以逗号分隔保存
    @Comment("选中用户的开始时间")
    private Date startDate;
    @Comment("选中用户的结束时间")
    private Date endDate;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @OneToMany(fetch = FetchType.LAZY)
    public List<Member> getMembers() {
        return members;
    }

    @Enumerated(EnumType.STRING)
    public RedPacketCategory getCategory() {
        return category;
    }

    @Enumerated(EnumType.STRING)
    public RedPacketSendType getSendType() {
        return sendType;
    }
}
