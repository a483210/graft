package com.psd.graft.core;

import com.psd.graft.annotation.PostEvent;
import com.psd.graft.annotation.Subscribe;
import com.psd.graft.error.GraftException;
import com.psd.graft.loader.SubscribeLoader;
import com.psd.graft.loader.SubscribeValue;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.log4j.Log4j2;

/**
 * 订阅处理
 *
 * @author Created by gold on 2019/10/11 09:49
 */
@Log4j2
public class SubscribeProcessor implements ApplicationContextAware {

    private SubscribeLoader loader;

    private Set<Class<? extends Annotation>> defAnnotations;

    public SubscribeProcessor(SubscribeLoader loader) {
        this.loader = loader;

        this.defAnnotations = new HashSet<>();

        registerAnnotation();
    }

    private void registerAnnotation() {
        defAnnotations.add(Component.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        List<RegisterValue> registers = findRegister(applicationContext);
        Map<String, EventValue> events = findPostEvent(registers);

        processSubscribe(registers, events);
        checkPostEvent(events);
    }

    private void processSubscribe(List<RegisterValue> registers, Map<String, EventValue> events) {
        for (RegisterValue register : registers) {
            for (Method method : register.cls.getMethods()) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                if (annotation == null) {
                    continue;
                }

                Type[] parameterTypes = method.getGenericParameterTypes();
                if (parameterTypes.length <= 0 || parameterTypes.length > 2) {
                    throw new GraftException(String.format("被%s注解的方法参数由Result和(非必须)Error组成！", Subscribe.class.getSimpleName()));
                }

                Class<?> cls = (Class<?>) parameterTypes[0];

                String event = getEvent(annotation.value(), cls.getName());
                if (!events.containsKey(event)) {
                    throw new GraftException(String.format("%s#%s没有事件可以订阅！", register.cls.getName(), method.getName()));
                }

                events.get(event).cut();

                SubscribeValue subscribeValue = new SubscribeValue(annotation.value(), register.beanName, method.getName());

                subscribeValue.setCls(cls);
                if (parameterTypes.length == 2) {
                    Class<?> errorCls = ((Class<?>) parameterTypes[1]);
                    if (!Throwable.class.isAssignableFrom(errorCls)) {
                        throw new GraftException(String.format("被%s注解的方法第二个参数非异常类型！", Subscribe.class.getSimpleName()));
                    }

                    subscribeValue.setThrowableCls((Class<? extends Throwable>) errorCls);
                }

                loader.putSubscribe(subscribeValue);
            }
        }
    }

    private void checkPostEvent(Map<String, EventValue> events) {
        for (Map.Entry<String, EventValue> entry : events.entrySet()) {
            EventValue eventValue = entry.getValue();
            if (eventValue.cut == 0) {
                RegisterValue register = eventValue.register;

                log.warn("{}#{}没有任何订阅！", register.cls.getName(), eventValue.method.getName());
            }
        }
    }

    private List<RegisterValue> findRegister(ApplicationContext applicationContext) {
        List<RegisterValue> registers = new ArrayList<>();

        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> cls = AopUtils.getTargetClass(bean);

            if (!checkAnnotation(cls)) {
                continue;
            }

            registers.add(new RegisterValue(beanName, bean, cls));
        }

        return registers;
    }

    private Map<String, EventValue> findPostEvent(List<RegisterValue> registers) {
        Map<String, EventValue> events = new HashMap<>();

        for (RegisterValue register : registers) {
            for (Method method : register.cls.getMethods()) {
                PostEvent annotation = method.getAnnotation(PostEvent.class);
                if (annotation == null) {
                    continue;
                }

                String event = getEvent(annotation.value(), method.getReturnType().getName());
                EventValue value = new EventValue(register, method);
                if (events.put(event, value) != null) {
                    throw new GraftException(String.format("%s#%s事件重复，请指定Tag！", register.cls.getName(), method.getName()));
                }
            }
        }

        return events;
    }

    private String getEvent(String tag, String clsName) {
        return String.format("%s_%s", tag, clsName);
    }

    private boolean checkAnnotation(Class<?> cls) {
        Annotation[] annotations = cls.getAnnotations();
        if (annotations.length == 0) {
            return false;
        }

        for (Annotation a : annotations) {
            Class<? extends Annotation> aCls = a.annotationType();
            if (defAnnotations.contains(aCls)) {
                return true;
            }

            Annotation[] childAs = aCls.getAnnotations();
            if (childAs.length == 0) {
                continue;
            }

            for (Annotation childA : childAs) {
                if (childA.annotationType() != Component.class) {
                    continue;
                }

                defAnnotations.add(aCls);

                return true;
            }
        }

        return false;
    }

    static class RegisterValue {
        String beanName;
        Object bean;
        Class<?> cls;

        RegisterValue(String beanName, Object bean, Class<?> cls) {
            this.beanName = beanName;
            this.bean = bean;
            this.cls = cls;
        }
    }

    static class EventValue {
        int cut;
        RegisterValue register;
        Method method;

        EventValue(RegisterValue register, Method method) {
            this.register = register;
            this.method = method;
        }

        void cut() {
            cut++;
        }
    }

}
