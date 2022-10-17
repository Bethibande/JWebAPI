package com.bethibande.web.processors;


import com.bethibande.web.WebRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ParameterProcessor {

    void process(WebRequest request, int parameterIndex, Method method, Parameter parameter);

}
