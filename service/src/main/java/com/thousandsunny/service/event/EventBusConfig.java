package com.thousandsunny.service.event;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * 创建AsyncEventBus,EventBus
 * Created by mu.jie on 2017/2/24.
 */
@Configuration
@ComponentScan(basePackages = {"guava"})
public class EventBusConfig {
    @Bean
    public AsyncEventBus asyncEventBus() {
        return new AsyncEventBus(Executors.newFixedThreadPool(100));
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }
}
