package com.psd.graft.impl;

import com.psd.graft.annotation.Subscribe;

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
    public void graft1(String result) {
        log.info("graft " + result);
    }

    @Subscribe
    public void graft2(String result, Throwable throwable) {
        if (throwable != null) {
            log.info("graft throwable " + throwable.toString());
        } else {
            log.info("graft noError " + result);
        }
    }

    @Subscribe
    public void graft3(String result, IllegalStateException e) {
        if (e != null) {
            log.info("graft e " + e.toString());
        } else {
            log.info("graft noError " + result);
        }
    }

}