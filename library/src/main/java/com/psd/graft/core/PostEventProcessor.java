package com.psd.graft.core;

import com.psd.graft.annotation.PostEvent;
import com.psd.graft.loader.SubscribeLoader;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * postEvent处理
 *
 * @author Created by gold on 2019/10/11 09:53
 */
@Aspect
public class PostEventProcessor {

    private SubscribeLoader loader;

    public PostEventProcessor(SubscribeLoader loader) {
        this.loader = loader;
    }

    @Pointcut("@annotation(com.psd.graft.annotation.PostEvent)")
    public void postPointCut() {
    }

    @Around("postPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        Method method = ((MethodSignature) point.getSignature()).getMethod();

        PostEvent postEvent = method.getAnnotation(PostEvent.class);

        Object result;
        try {
            loader.executeParamSubscribe(postEvent.value(), point.getArgs());

            result = point.proceed();

            loader.executeSubscribe(postEvent.value(), result);
        } catch (Throwable e) {
            loader.executeSubscribe(postEvent.value(), method.getReturnType(), e);
            throw e;
        }

        return result;
    }

}