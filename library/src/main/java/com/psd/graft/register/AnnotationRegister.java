package com.psd.graft.register;

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 注解注册类
 *
 * @author Created by gold on 2019/10/15 17:44
 */
public class AnnotationRegister {

    public static AnnotationRegister create(ApplicationContext applicationContext) {
        return new AnnotationRegister(applicationContext);
    }

    private ApplicationContext applicationContext;
    private Set<Class<? extends Annotation>> defAnnotations;
    private Set<Class<? extends Annotation>> unAnnotations;

    public AnnotationRegister(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        this.defAnnotations = new HashSet<>();
        this.unAnnotations = new HashSet<>();

        registerAnnotation();
    }

    private void registerAnnotation() {
        defAnnotations.add(Component.class);
    }

    public List<RegisterValue> findRegister() {
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

    private boolean checkAnnotation(Class<?> cls) {
        Annotation[] annotations = cls.getAnnotations();
        if (annotations.length == 0) {
            return false;
        }

        for (Annotation a : annotations) {
            Class<? extends Annotation> aCls = a.annotationType();
            if (defAnnotations.contains(aCls)) {
                return true;
            } else if (unAnnotations.contains(aCls)) {
                return false;
            } else if (checkAnnotationCls(aCls)) {
                defAnnotations.add(aCls);
                return true;
            } else {
                unAnnotations.add(aCls);
            }
        }

        return false;
    }

    private boolean checkAnnotationCls(Class<? extends Annotation> cls) {
        Annotation[] annotations = cls.getAnnotations();
        if (annotations.length == 0) {
            return false;
        }

        for (Annotation a : annotations) {
            Class<? extends Annotation> aCls = a.annotationType();
            if (isSystemClass(aCls.getName())) {
                continue;
            }

            if (checkAnnotationComponent(aCls)) {
                return true;
            } else {
                if (checkAnnotationCls(aCls)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean checkAnnotationComponent(Class<? extends Annotation> cls) {
        if (cls == Component.class) {
            return true;
        } else if (defAnnotations.contains(cls)) {
            return true;
        } else if (unAnnotations.contains(cls)) {
            return false;
        }

        Annotation[] annotations = cls.getAnnotations();
        if (annotations.length == 0) {
            return false;
        }

        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == Component.class) {
                defAnnotations.add(cls);
                return true;
            }
        }

        return false;
    }

    private static boolean isSystemClass(String name) {
        return name.startsWith("java.") || name.startsWith("javax.");
    }
}
