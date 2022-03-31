package de.bethibande.web.handlers;

import de.bethibande.web.annotations.JsonMappings;
import de.bethibande.web.annotations.URI;
import de.bethibande.web.reflect.ClassUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class HandlerManager {

    private final HashMap<String, HandlerInstance> handlers = new HashMap<>();

    public void registerHandler(Class<? extends WebHandler> handler) {
        if(!handler.isAnnotationPresent(URI.class)) {
            System.err.println("Class handler without @URI annotation: " + handler.getName());
            return;
        }

        String classUri = handler.getAnnotation(URI.class).value();
        Object instance = ClassUtils.createClassInstance(handler);
        Method[] methods = handler.getDeclaredMethods();
        HashMap<String, MethodHandle> methodHandles = new HashMap<>();

        for(Method m : methods) {
            if(!m.isAnnotationPresent(URI.class)) continue;
            String uri = m.getAnnotation(URI.class).value();
            HandleType inputType = HandleType.DEFAULT;
            HandleType outputType = HandleType.DEFAULT;
            if(m.getReturnType().isAssignableFrom(InputStream.class)) outputType = HandleType.STREAM;

            if(m.isAnnotationPresent(JsonMappings.class)) inputType = HandleType.JSON;
            for(Class<?> t : m.getParameterTypes()) {
                if(t == InputStream.class) inputType = HandleType.STREAM;
            }

            MethodHandle handle = new MethodHandle(uri, m, Modifier.isStatic(m.getModifiers()), inputType, outputType);
            methodHandles.put(uri, handle);
        }

        handlers.put(classUri, new HandlerInstance(classUri, instance, methodHandles));
    }

    public HashMap<String, HandlerInstance> getHandlers() {
        return handlers;
    }
}
