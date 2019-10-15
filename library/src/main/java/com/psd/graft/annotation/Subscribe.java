package com.psd.graft.annotation;

import com.psd.graft.event.EventType;
import com.psd.graft.event.UseType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 订阅事件
 *
 * @author Created by gold on 2019/10/11 09:36
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * tag区分
     */
    String value() default EventType.DEFAULT_TAG;

    /**
     * 默认使用出参
     * <p>
     * 使用入参和异常注意tag使用，编译器不会做检查
     */
    UseType useFor() default UseType.RESULT;

}