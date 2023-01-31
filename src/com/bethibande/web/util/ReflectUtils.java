package com.bethibande.web.util;

import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.types.ProcessorMappings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;

public class ReflectUtils {

    public static HashMap<Class<?>, ProcessorMappings> mappings = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T autoWireNewInstance(Class<T> type, ServerContext context) {
        Constructor<T> constructor = (Constructor<T>) type.getConstructors()[0];
        Parameter[] parameters = constructor.getParameters();
        Object[] values = new Object[parameters.length];
        ProcessorMappings mappings = ReflectUtils.mappings.get(type);
        if(mappings == null) {
            mappings = ProcessorMappings.of(constructor, context.api());
            ReflectUtils.mappings.put(type, mappings);
        }

        ParameterProcessor[] processors = mappings.getProcessors();

        for(int i = 0; i < parameters.length; i++) {
            values[i] = processors[i].process(context, constructor, parameters[i]);
        }

        try {
            constructor.setAccessible(true);
            return constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<T> type) {
        try {
            return (T) type.getDeclaredConstructors()[0].newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
