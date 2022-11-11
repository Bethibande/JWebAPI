package com.bethibande.web.beans;

import com.bethibande.web.annotations.PostConstruct;
import com.bethibande.web.annotations.PostDestroy;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.util.ReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class BeanManager {

    private BeanFactory factory;

    private final HashMap<Class<? extends Bean>, BeanSnapshot> beans = new HashMap<>();
    private final HashMap<Class<? extends Bean>, Bean> activeBeans = new HashMap<>();

    public BeanManager() {
        init();
    }

    private void init() {
        this.factory = new BeanFactory() {
            @Override
            public <T extends Bean> T create(Class<T> type, ServerContext context) {
                return ReflectUtils.autoWireNewInstance(type, context);
            }
        };
    }

    public void activate(Bean bean) {
        activeBeans.put(bean.getClass(), bean);
    }

    public void storeActiveBeans() {
        activeBeans.forEach((type, bean) -> this.storeBean(bean));
    }

    private void invokeAnnotatedMethod(Object obj, Class<? extends Annotation> annotation) {
        for(Method method : obj.getClass().getDeclaredMethods()) {
            if(!method.isAnnotationPresent(annotation)) continue;

            method.setAccessible(true);
            try {
                method.invoke(obj, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            break;
        }
    }

    public <T extends Bean> void storeBean(T obj) {
        HashMap<Field, Object> state = new HashMap<>();
        Class<? extends Bean> type = obj.getClass();
        for(Field field : type.getDeclaredFields()) {
            if(Modifier.isStatic(field.getModifiers())) continue;
            if(Modifier.isTransient(field.getModifiers())) continue;

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                state.put(field, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        beans.remove(type);
        beans.put(type, new BeanSnapshot(
                type,
                state
        ));
    }

    public void deleteBean(Class<? extends Bean> type, ServerContext con) {
        Object bean = getBean(type, con);
        invokeAnnotatedMethod(bean, PostDestroy.class);
        beans.remove(type);
    }

    public <T extends Bean> T getBean(Class<T> type, ServerContext context) {
        if(beans.containsKey(type)) return getBeanFromSnapshot(type, context);
        return createBean(type, context);
    }

    private <T extends Bean
            > T getBeanFromSnapshot(Class<T> type, ServerContext context) {
        T bean = createBean(type, context);
        BeanSnapshot snapshot = beans.get(type);
        HashMap<Field, Object> state = snapshot.getState();

        for(Field field : state.keySet()) {
            field.setAccessible(true);
            try {
                field.set(bean, state.get(field));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return bean;
    }

    public BeanFactory getFactory() {
        return factory;
    }

    public void setFactory(BeanFactory factory) {
        this.factory = factory;
    }

    private <T extends Bean> T createBean(Class<T> type, ServerContext context) {
        T bean = factory.create(type, context);
        invokeAnnotatedMethod(bean, PostConstruct.class);

        if(!beans.containsKey(type)) {
            beans.put(type, null);
        }

        return bean;
    }

}
