package com.psd.graft.impl;

import com.psd.graft.annotation.Subscribe;
import com.psd.graft.event.UseType;

import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * 消费者
 *
 * @author Created by gold on 2019/10/5 14:34
 */
@Log4j2
@Component
public class GraftSubscribe {

    @Subscribe
    public void subscribeGraft(String result) {
        log.info("subscribeGraft " + result);
    }

    @Subscribe
    public void subscribeGraftThrowable(String result, Throwable throwable) {
        if (throwable != null) {
            log.info("subscribeGraftThrowable t " + throwable.toString());
        } else {
            log.info("subscribeGraftThrowable " + result);
        }
    }

    @Subscribe
    public void subscribeGraftException(String result, IllegalStateException e) {
        if (e != null) {
            log.info("subscribeGraftException e " + e.toString());
        } else {
            log.info("subscribeGraftException " + result);
        }
    }

    @Subscribe(useFor = UseType.ERROR)
    public void subscribeGraftSingleThrowable(Throwable throwable) {
        log.info("subscribeGraftSingleThrowable t " + throwable);
    }

    @Subscribe(useFor = UseType.PARAM)
    public void subscribeGraftParam(String param) {
        log.info("subscribeGraftParam " + param);
    }
}