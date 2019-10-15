package com.psd.graft.loader;

import com.psd.graft.event.UseType;

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
    private Map<String, Map<String, List<SubscribeValue>>> loader;

    public SubscribeLoader() {
        this.loader = new HashMap<>();
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
        Map<String, List<SubscribeValue>> map = loader
                .computeIfAbsent(subscribeValue.getTag(), m -> new HashMap<>());

        String name = subscribeValue.getCls().getName();
        if (subscribeValue.getUseFor() == UseType.PARAM) {
            name = generateParamForName(name);
        } else if (subscribeValue.getUseFor() == UseType.ERROR) {
            name = generateThrowableForName();

        }
        List<SubscribeValue> list = map.computeIfAbsent(name, l -> new ArrayList<>());

        list.add(subscribeValue);
    }

    private String generateParamForName(String name) {
        return String.format("paramFor%s", name);
    }

    private String generateThrowableForName() {
        return "throwableFor";
    }

    private List<SubscribeValue> getSubscribe(String tag, Class<?> cls) {
        return getSubscribe(tag, cls.getName());
    }

    /**
     * 获取订阅信息
     *
     * @param tag  标签
     * @param name 类型
     */
    private List<SubscribeValue> getSubscribe(String tag, String name) {
        Map<String, List<SubscribeValue>> map = loader.get(tag);
        if (map == null) {
            return null;
        }

        return map.get(name);
    }

    /**
     * 执行入参订阅
     *
     * @param tag  标记
     * @param args 入参
     */
    public void executeParamSubscribe(String tag, Object[] args) {
        String name = generateParamForName(args[0].getClass().getName());

        List<SubscribeValue> subscribeValues = getSubscribe(tag, name);
        if (CollectionUtils.isEmpty(subscribeValues)) {
            return;
        }

        for (SubscribeValue subscribeValue : subscribeValues) {
            Object bean = applicationContext.getBean(subscribeValue.getBeanName());
            if (bean == null) {
                continue;
            }

            executeMethod(subscribeValue, bean, args, null);
        }
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
        executeThrowableSubscribe(tag, throwable);

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

    //执行单异常订阅
    private void executeThrowableSubscribe(String tag, Throwable throwable) {
        String name = generateThrowableForName();

        List<SubscribeValue> subscribeValues = getSubscribe(tag, name);
        if (CollectionUtils.isEmpty(subscribeValues)) {
            return;
        }

        for (SubscribeValue subscribeValue : subscribeValues) {
            if (subscribeValue.getUseFor() != UseType.ERROR) {
                continue;
            }
            Object bean = applicationContext.getBean(subscribeValue.getBeanName());
            if (bean == null) {
                continue;
            }

            executeMethod(subscribeValue, bean, throwable, null);
        }
    }

    private void executeMethod(SubscribeValue subscribeValue, Object bean, Object value, Throwable throwable) {
        try {
            Method method;
            Class<?> beanCls = bean.getClass();
            Class<?> cls = subscribeValue.getCls();
            Class<? extends Throwable> throwableCls = subscribeValue.getThrowableCls();

            if (subscribeValue.getUseFor() == UseType.PARAM) {
                Object[] args = ((Object[]) value);
                Class<?>[] types = new Class[args.length];
                for (int i = 0; i < args.length; i++) {
                    types[i] = args[i].getClass();
                }

                method = beanCls.getMethod(subscribeValue.getMethodName(), types);

                method.invoke(bean, args);
            } else if (throwableCls == null) {
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
