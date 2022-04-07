package com.bethibande.web.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ClassUtils {

    public static Object generateDefaultValue(Class<?> type) {
        if(type == Boolean.class || type == boolean.class) return false;
        if(type == Integer.class || type == int.class) return 0;
        if(type == Byte.class || type == byte.class) return (byte)0;
        if(type == Short.class || type == short.class) return (short)0;
        if(type == Long.class || type == long.class) return 0L;
        if(type == Double.class || type == double.class) return (double)0;
        if(type == Float.class || type == float.class) return (float)0;

        return null;
    }

    public static <T> T createClassInstance(Class<T> type) {
        Constructor<T> con = (Constructor<T>) type.getConstructors()[0];
        Object[] parameters = new Object[con.getParameters().length];
        con.setAccessible(true);

        for(int i = 0; i < parameters.length; i++) {
            Class<?> parameter = con.getParameterTypes()[i];
            parameters[i] = generateDefaultValue(parameter);
        }

        T instance = null;
        try {
            instance = con.newInstance(parameters);
        } catch(IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }

        return instance;
    }

}
