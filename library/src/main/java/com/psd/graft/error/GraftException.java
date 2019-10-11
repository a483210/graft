package com.psd.graft.error;

import lombok.Getter;

/**
 * graft异常
 *
 * @author Created by gold on 2019/10/11 12:24
 */
@Getter
public class GraftException extends RuntimeException {

    public GraftException(String message) {
        super(message);
    }

}
