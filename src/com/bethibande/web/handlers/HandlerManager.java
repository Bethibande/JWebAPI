package com.bethibande.web.handlers;

import com.bethibande.web.annotations.JsonMappings;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.regex.RegexMatcher;
import com.bethibande.web.reflect.ClassUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

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
                if(t == InputStream.class) {
                    inputType = HandleType.STREAM;
                    break;
                }
            }

            String jointUri = (classUri + "/" + uri).replaceAll("//|///", "/");
            Integer[] indexes = RegexMatcher.getIndexes(jointUri);
            Map<Integer, FieldHandle> handles = RegexMatcher.getUriFields(jointUri, indexes);

            MethodHandle handle = new MethodHandle(uri, m, Modifier.isStatic(m.getModifiers()), handles, inputType, outputType);
            methodHandles.put(handle.getUri() /* not the same as the uri passed into the constructor */, handle);
        }

        handlers.put(classUri, new HandlerInstance(classUri, instance, methodHandles));
    }

    public HashMap<String, HandlerInstance> getHandlers() {
        return handlers;
    }
}
