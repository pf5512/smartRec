package com.thousandsunny.service.event;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.thousandsunny.core.model.Member;
import com.thousandsunny.service.model.RedPacket;
import com.thousandsunny.service.model.RedPacketReceive;
import com.thousandsunny.service.repository.RedPacketReceiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mu.jie on 2017/2/24.
 */
@Component
public class RedPacketEvent {
    @Autowired
    private RedPacketReceiveRepository redPacketReceiveRepository;

    @Autowired
    public RedPacketEvent(AsyncEventBus asyncEventBus) {
        asyncEventBus.register(this);
    }

    //@Subscribe注解，来指定sendRedPacket作为事件处理方法,@AllowConcurrentEvents线程安全
    @AllowConcurrentEvents
    @Subscribe
    public void sendRedPacket(RedPacketReceive redPacketReceive) {
        //这里执行保存红包功能
        redPacketReceiveRepository.save(redPacketReceive);
    }

}
