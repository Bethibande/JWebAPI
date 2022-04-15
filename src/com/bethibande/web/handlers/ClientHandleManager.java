package com.bethibande.web.handlers;

import com.bethibande.web.annotations.JsonMappings;
import com.bethibande.web.annotations.QueryField;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.response.StreamResponse;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientHandleManager {

    private final Class<?> clazz;
    private final HashMap<Method, ClientHandle> methods;

    public ClientHandleManager(Class<?> clazz, HashMap<Method, ClientHandle> methods) {
        this.clazz = clazz;
        this.methods = methods;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public HashMap<Method, ClientHandle> getMethods() {
        return methods;
    }

    public ClientHandle get(Method m) {
        return methods.get(m);
    }

    public static ClientHandleManager of(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        HashMap<Method, ClientHandle> handles = new HashMap<>();

        for(Method method : methods) {
            if(!method.isAnnotationPresent(URI.class)) continue;
            String uri = method.getAnnotation(URI.class).value();
            List<Integer> query = new ArrayList<>();
            HandleType inputType = HandleType.DEFAULT;
            if(method.isAnnotationPresent(JsonMappings.class)) inputType = HandleType.JSON;

            for(int i = 0; i < method.getParameterTypes().length; i++) {
                Class<?> t = method.getParameterTypes()[i];
                Parameter p = method.getParameters()[i];

                if(p.isAnnotationPresent(QueryField.class)) query.add(i);
                if(t == StreamResponse.class) inputType = HandleType.STREAM;
            }

            handles.put(method, new ClientHandle(method, inputType, uri, query));
        }

        return new ClientHandleManager(clazz, handles);
    }

}
