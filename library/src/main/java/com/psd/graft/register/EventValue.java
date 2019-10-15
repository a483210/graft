package com.psd.graft.register;

import java.lang.reflect.Method;

import lombok.Data;

/**
 * 事件信息
 *
 * @author Created by gold on 2019/10/15 17:46
 */
@Data
public class EventValue {

    private int cut;
    private RegisterValue register;
    private Method method;

    public EventValue(RegisterValue register, Method method) {
        this.register = register;
        this.method = method;
    }

    public void cut() {
        cut++;
    }

}