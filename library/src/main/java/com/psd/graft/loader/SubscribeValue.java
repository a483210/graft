package com.psd.graft.loader;

import com.psd.graft.event.UseType;

import lombok.Data;

/**
 * 订阅信息
 *
 * @author Created by gold on 2019/10/11 09:56
 */
@Data
public class SubscribeValue {

    private String tag;

    private String beanName;
    private String methodName;

    private Class<?> cls;
    private Class<? extends Throwable> throwableCls;

    private UseType useFor;

    public SubscribeValue(String tag, String beanName, String methodName) {
        this.tag = tag;
        this.beanName = beanName;
        this.methodName = methodName;
    }

}
