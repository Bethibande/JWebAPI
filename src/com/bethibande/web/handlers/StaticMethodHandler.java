package com.bethibande.web.handlers;

import com.bethibande.web.WebRequest;
import com.bethibande.web.response.RequestResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StaticMethodHandler extends MethodHandler {

    public StaticMethodHandler(Method method) {
        super(method);
    }

    @Override
    public RequestResponse invoke(WebRequest request) {
        super.prepare(request);

        try {
            Object value = getMethod().invoke(null, request.getMethodInvocationParameters());
            if(value != null) request.getResponse().setContentData(value);
        } catch(IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return request.getResponse();
    }
}
