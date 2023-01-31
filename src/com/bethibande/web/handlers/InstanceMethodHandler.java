package com.bethibande.web.handlers;

import com.bethibande.web.JWebServer;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.types.ProcessorMappings;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.processors.MethodInvocationHandler;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.util.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InstanceMethodHandler extends MethodHandler {

    private final Object instance;

    public InstanceMethodHandler(Method method, ProcessorMappings mappings) {
        super(method, mappings);

        this.instance = ReflectUtils.createInstance(method.getDeclaringClass());
    }

    @Override
    public RequestResponse invoke(ServerContext context) {
        final WebRequest request = context.request();
        final JWebServer server = context.api();
        final Method method = getMethod();

        for(MethodInvocationHandler handler : server.getMethodInvocationHandlers()) {
            if(request.isFinished()) break;

            handler.beforeInvocation(method, request, server);
        }
        if(request.isFinished()) return request.getResponse();

        super.prepare(context);

        try {
            Object value = getMethod().invoke(instance, request.getMethodInvocationParameters());
            request.getResponse().setContentData(value);
        } catch(IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        for(MethodInvocationHandler handler : server.getMethodInvocationHandlers()) {
            handler.afterInvocation(method, request, server);
        }

        return request.getResponse();
    }
}
