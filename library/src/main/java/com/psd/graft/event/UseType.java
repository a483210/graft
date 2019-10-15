package com.psd.graft.event;

import lombok.Getter;

/**
 * 使用类型
 *
 * @author Created by gold on 2019/10/15 18:35
 */
@Getter
public enum UseType {

    /**
     * 使用返回值
     */
    RESULT(0),
    /**
     * 使用入参
     */
    PARAM(1),
    /**
     * 使用异常
     */
    ERROR(2);

    private int type;

    UseType(int type) {
        this.type = type;
    }
}
