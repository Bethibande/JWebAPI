package com.bethibande.web.beans;

import com.bethibande.web.annotations.PostConstruct;
import com.bethibande.web.annotations.PostDestroy;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class GlobalBeanManager {

    private final HashMap<Class<? extends GlobalBean>, GlobalBean> beans = new HashMap<>();

    private void invokeAnnotatedMethod(Object obj, Class<? extends Annotation> annotation) {
        for(Method method : obj.getClass().getDeclaredMethods()) {
            if(!method.isAnnotationPresent(annotation)) continue;

            method.setAccessible(true);
            try {
                method.invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            break;
        }
    }

    public void storeBean(GlobalBean bean) {
        beans.remove(bean.getClass());
        beans.put(bean.getClass(), bean);

        invokeAnnotatedMethod(bean, PostConstruct.class);
    }

    public <T extends GlobalBean> T getBean(Class<T> type) {
        Object bean = beans.get(type);
        if(bean == null) return null;
        return (T)bean;
    }

    public <T extends GlobalBean> void deleteBean(Class<T> type) {
        GlobalBean bean = beans.remove(type);
        invokeAnnotatedMethod(bean, PostDestroy.class);
    }

}
