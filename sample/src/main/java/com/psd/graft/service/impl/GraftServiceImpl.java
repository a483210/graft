package com.psd.graft.service.impl;

import com.psd.graft.annotation.PostEvent;
import com.psd.graft.service.GraftService;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * graft服务实现
 *
 * @author Created by gold on 2019/10/11 09:34
 */
@Service
public class GraftServiceImpl implements GraftService {

    @PostEvent
    @Override
    public String graftPostEvent(String content) {
        if (content.equals("failure")) {
            throw new IllegalStateException("failure");
        }
        return String.format("%s-%s", content, UUID.randomUUID().toString());
    }

}
