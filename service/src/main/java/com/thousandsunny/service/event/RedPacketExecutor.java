package com.thousandsunny.service.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.thousandsunny.core.ModuleKey;
import com.thousandsunny.core.domain.repository.MemberRepository;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.RedPacket;
import com.thousandsunny.service.model.RedPacketReceive;
import com.thousandsunny.service.repository.RedPacketRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.RdbmsOperation;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.thousandsunny.service.ModuleKey.RedPacketCategory.RED_PACKET_COURSE_TRAIN;

/**
 * Created by mu.jie on 2017/2/24.
 */
@Data
@Component
public class RedPacketExecutor {
    private AsyncEventBus asyncEventBus;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RedPacketRepository redPacketRepository;

    @Autowired
    public RedPacketExecutor(AsyncEventBus asyncEventBus) {
        this.asyncEventBus = asyncEventBus;
    }

    public void executeRedPacket(List<Member> members, RedPacket redPacket) {
        //创建红包
        members.forEach(member -> {
            RedPacketReceive redPacketReceive = new RedPacketReceive();
            redPacketReceive.setMember(member);
            redPacketReceive.setRedPacket(redPacket);
            redPacketReceive.setValidDate(redPacket.getValidDate());
            asyncEventBus.post(redPacketReceive);
        });
    }

}
