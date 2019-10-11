package com.psd.graft;

import com.psd.graft.core.PostEventProcessor;
import com.psd.graft.core.SubscribeProcessor;
import com.psd.graft.loader.SubscribeLoader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * 初始化订阅方法
 *
 * @author Created by gold on 2019/10/11 09:48
 */
@Slf4j
@Configuration
public class GraftAutoConfiguration {

    @Bean
    public SubscribeLoader loader() {
        return new SubscribeLoader();
    }

    @Bean
    public PostEventProcessor postEvent(SubscribeLoader loader) {
        return new PostEventProcessor(loader);
    }

    @Bean
    public SubscribeProcessor subscriber(SubscribeLoader loader) {
        return new SubscribeProcessor(loader);
    }

}
