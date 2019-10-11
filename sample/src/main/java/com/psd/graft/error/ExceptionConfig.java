package com.psd.graft.error;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常配置类
 *
 * @author Created by gold on 2019/10/11 14:37
 */
@Slf4j
@ControllerAdvice
public class ExceptionConfig {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(HttpServletRequest request, Exception e) {
        log.error("exception error:", e);

        return e.getMessage();
    }

}