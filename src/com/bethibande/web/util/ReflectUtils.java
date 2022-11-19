package com.bethibande.web.util;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.ParameterProcessor;
import com.bethibande.web.types.WebRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class ReflectUtils {

    @SuppressWarnings("unchecked")
    public static <T> T autoWireNewInstance(Class<T> type, ServerContext context) {
        JWebServer server = context.server();
        WebRequest request = context.request().createClone();
        Constructor<T> constructor = (Constructor<T>) type.getConstructors()[0];
        Parameter[] parameters = constructor.getParameters();

        request.setMethodInvocationParameters(new Object[parameters.length]);

        for(int i = 0; i < parameters.length; i++) {
            for(ParameterProcessor processor : server.getProcessors()) {
                processor.process(context, i, constructor, parameters[i]);
            }
        }

        try {
            constructor.setAccessible(true);
            return constructor.newInstance(request.getMethodInvocationParameters());
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
