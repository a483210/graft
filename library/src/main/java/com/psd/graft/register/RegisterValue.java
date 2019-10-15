package com.psd.graft.register;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 注册信息
 *
 * @author Created by gold on 2019/10/15 17:46
 */
@Data
@AllArgsConstructor
public class RegisterValue {

    private String beanName;
    private Object bean;
    private Class<?> cls;

}
