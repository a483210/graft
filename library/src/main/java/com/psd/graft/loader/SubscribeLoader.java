package com.psd.graft.loader;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

/**
 * 订阅加载器
 *
 * @author Created by gold on 2019/10/11 09:54
 */
@Log4j2
public class SubscribeLoader implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private Map<String, Map<Class<?>, List<SubscribeValue>>> loader;

    public SubscribeLoader() {
        loader = new HashMap<>();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 添加订阅信息
     *
     * @param subscribeValue 订阅信息
     */
    public void putSubscribe(SubscribeValue subscribeValue) {
        Map<Class<?>, List<SubscribeValue>> map = loader
                .computeIfAbsent(subscribeValue.getTag(), m -> new HashMap<>());

        List<SubscribeValue> list = map.computeIfAbsent(subscribeValue.getCls(), l -> new ArrayList<>());

        list.add(subscribeValue);
    }

    /**
     * 获取订阅信息
     *
     * @param tag 标签
     * @param cls 类型
     */
    private List<SubscribeValue> getSubscribe(String tag, Class<?> cls) {
        Map<Class<?>, List<SubscribeValue>> map = loader.get(tag);
        if (map == null) {
            return null;
        }

        return map.get(cls);
    }

    /**
     * 执行订阅
     *
     * @param tag   标记
     * @param value 返回值
     */
    public void executeSubscribe(String tag, Object value) {
        List<SubscribeValue> subscribeValues = getSubscribe(tag, value.getClass());
        if (CollectionUtils.isEmpty(subscribeValues)) {
            return;
        }

        for (SubscribeValue subscribeValue : subscribeValues) {
            Object bean = applicationContext.getBean(subscribeValue.getBeanName());
            if (bean == null) {
                continue;
            }

            executeMethod(subscribeValue, bean, value, null);
        }
    }

    /**
     * 执行异常订阅
     *
     * @param tag 标记
     * @param cls 返回值类型
     */
    public void executeSubscribe(String tag, Class<?> cls, Throwable throwable) {
        List<SubscribeValue> subscribeValues = getSubscribe(tag, cls);
        if (subscribeValues == null) {
            return;
        }

        for (SubscribeValue subscribeValue : subscribeValues) {
            Class<? extends Throwable> throwableCls = subscribeValue.getThrowableCls();
            if (throwableCls == null) {
                continue;
            }
            if (!throwableCls.isAssignableFrom(throwable.getClass())) {
                continue;
            }

            Object bean = applicationContext.getBean(subscribeValue.getBeanName());
            if (bean == null) {
                continue;
            }

            executeMethod(subscribeValue, bean, null, throwable);
        }
    }

    private void executeMethod(SubscribeValue subscribeValue, Object bean, Object value, Throwable throwable) {
        try {
            Method method;
            Class<?> beanCls = bean.getClass();
            Class<?> cls = subscribeValue.getCls();
            Class<? extends Throwable> throwableCls = subscribeValue.getThrowableCls();

            if (throwableCls == null) {
                method = beanCls.getMethod(subscribeValue.getMethodName(), cls);

                method.invoke(bean, value);
            } else {
                method = beanCls.getMethod(subscribeValue.getMethodName(), cls, throwableCls);

                method.invoke(bean, value, throwable);
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }
}
