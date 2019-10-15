package com.psd.graft.core;

import com.psd.graft.annotation.PostEvent;
import com.psd.graft.annotation.Subscribe;
import com.psd.graft.error.GraftException;
import com.psd.graft.event.UseType;
import com.psd.graft.loader.SubscribeLoader;
import com.psd.graft.loader.SubscribeValue;
import com.psd.graft.register.AnnotationRegister;
import com.psd.graft.register.EventValue;
import com.psd.graft.register.RegisterValue;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

/**
 * 订阅处理
 *
 * @author Created by gold on 2019/10/11 09:49
 */
@Log4j2
public class SubscribeProcessor implements ApplicationContextAware {

    private SubscribeLoader loader;

    public SubscribeProcessor(SubscribeLoader loader) {
        this.loader = loader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        List<RegisterValue> registers = AnnotationRegister.create(applicationContext).findRegister();
        Map<String, EventValue> events = findPostEvent(registers);

        processSubscribe(registers, events);
        checkPostEvent(events);
    }

    private void processSubscribe(List<RegisterValue> registers, Map<String, EventValue> events) {
        for (RegisterValue register : registers) {
            for (Method method : register.getCls().getMethods()) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                if (annotation == null) {
                    continue;
                }

                SubscribeValue subscribeValue = new SubscribeValue(annotation.value(), register.getBeanName(), method.getName());
                subscribeValue.setUseFor(annotation.useFor());

                Type[] parameterTypes = method.getGenericParameterTypes();

                if (annotation.useFor() != UseType.RESULT) {
                    if (parameterTypes.length == 0) {
                        throw new GraftException(String.format("被%s注解的方法参数不能为空！", Subscribe.class.getSimpleName()));
                    }

                    Class<?> cls = (Class<?>) parameterTypes[0];

                    subscribeValue.setCls(cls);
                } else {
                    if (parameterTypes.length <= 0 || parameterTypes.length > 2) {
                        throw new GraftException(String.format("被%s注解的方法参数由Result和(非必须)Error组成！", Subscribe.class.getSimpleName()));
                    }

                    Class<?> cls = (Class<?>) parameterTypes[0];

                    String event = getEvent(annotation.value(), cls.getName());
                    if (!events.containsKey(event)) {
                        throw new GraftException(String.format("%s#%s没有事件可以订阅！", register.getCls().getName(), method.getName()));
                    }

                    events.get(event).cut();

                    subscribeValue.setCls(cls);
                    if (parameterTypes.length == 2) {
                        Class<?> errorCls = ((Class<?>) parameterTypes[1]);
                        if (!Throwable.class.isAssignableFrom(errorCls)) {
                            throw new GraftException(String.format("被%s注解的方法第二个参数非异常类型！", Subscribe.class.getSimpleName()));
                        }

                        subscribeValue.setThrowableCls((Class<? extends Throwable>) errorCls);
                    }
                }

                loader.putSubscribe(subscribeValue);
            }
        }
    }

    private void checkPostEvent(Map<String, EventValue> events) {
        for (Map.Entry<String, EventValue> entry : events.entrySet()) {
            EventValue eventValue = entry.getValue();
            if (eventValue.getCut() == 0) {
                RegisterValue register = eventValue.getRegister();

                log.warn("{}#{}没有任何订阅！", register.getCls().getName(), eventValue.getMethod().getName());
            }
        }
    }

    private Map<String, EventValue> findPostEvent(List<RegisterValue> registers) {
        Map<String, EventValue> events = new HashMap<>();

        for (RegisterValue register : registers) {
            for (Method method : register.getCls().getMethods()) {
                PostEvent annotation = method.getAnnotation(PostEvent.class);
                if (annotation == null) {
                    continue;
                }

                String resultEvent = getEvent(annotation.value(), method.getReturnType().getName());
                EventValue resultValue = new EventValue(register, method);
                if (events.put(resultEvent, resultValue) != null) {
                    throw new GraftException(String.format("%s#%s事件重复，请指定Tag！", register.getCls().getName(), method.getName()));
                }
            }
        }

        return events;
    }

    private String getEvent(String tag, String clsName) {
        return String.format("%s_%s", tag, clsName);
    }

}
