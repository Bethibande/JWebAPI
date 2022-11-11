package com.bethibande.web.beans;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class BeanManager {

    private BeanFactory factory;

    private final HashMap<Class<?>, BeanSnapshot> beans = new HashMap<>();

    public BeanManager() {
        init();
    }

    private void init() {
        this.factory = new BeanFactory() {
            @Override
            public <T> T create(Class<T> type, ServerContext context) {
                return ReflectUtils.createInstance(type);
            }
        };
    }

    public void storeBean(Object obj) {
        HashMap<Field, Object> state = new HashMap<>();
        Class<?> type = obj.getClass();
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

    public <T> T getBean(Class<T> type, ServerContext context) {
        if(beans.containsKey(type)) return getBeanFromSnapshot(type, context);
        return createBean(type, context);
    }

    private <T> T getBeanFromSnapshot(Class<T> type, ServerContext context) {
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

    private <T> T createBean(Class<T> type, ServerContext context) {
        T bean = factory.create(type, context);

        if(!beans.containsKey(type)) {
            beans.put(type, null);
        }

        return bean;
    }

}
