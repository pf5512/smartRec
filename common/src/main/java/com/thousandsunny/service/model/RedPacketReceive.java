package com.thousandsunny.service.model;

import com.thousandsunny.common.entity.Comment;
import com.thousandsunny.core.ModuleKey.BooleanEnum;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.ModuleKey.RedPacketState;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

import static com.thousandsunny.core.ModuleKey.BooleanEnum.*;
import static com.thousandsunny.service.ModuleKey.RedPacketState.NORMAL;
import static javax.persistence.GenerationType.AUTO;

/**
 * Created by mu.jie on 2017/2/15.
 */
@Data
@Entity
@Comment("红包领取")
@Table(name = "sr_red_packet_receive")
public class RedPacketReceive {
    private Long id;
    @Comment("红包")
    private RedPacket redPacket;
    @Comment("用户")
    private Member member;
    @Comment("领取时间")
    private Date createTime = new Date();
    @Comment("过期时间")
    private Date validDate;
    @Comment("红包状态")
    private RedPacketState state = NORMAL;
    @Comment("是否已读")
    private BooleanEnum isRead = NO;

    @Id
    @GeneratedValue(strategy = AUTO)
    public Long getId() {
        return id;
    }

    @ManyToOne
    public RedPacket getRedPacket() {
        return redPacket;
    }

    @ManyToOne
    public Member getMember() {
        return member;
    }

    @Enumerated(EnumType.STRING)
    public RedPacketState getState() {
        return state;
    }

    @Enumerated(EnumType.STRING)
    public BooleanEnum getIsRead() {
        return isRead;
    }

}
