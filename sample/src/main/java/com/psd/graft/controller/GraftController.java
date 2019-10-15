package com.psd.graft.controller;

import com.psd.graft.service.GraftService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置测试
 *
 * @author Created by gold on 2019/9/25 11:09
 */
@RestController
@RequestMapping("/graft")
public class GraftController {

    @Autowired
    private GraftService graftService;

    @GetMapping(value = "/get/{content}")
    public String postEvent(@PathVariable("content") String content) {
        if (StringUtils.isEmpty(content)) {
            return "failure";
        }

        return graftService.graftPostEvent(content);
    }

}
