package com.bethibande.web.types;

import com.bethibande.web.JWebServer;
import com.bethibande.web.processors.ParameterProcessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ProcessorMappings {

    public static ProcessorMappings of(Method method, JWebServer parent) {
        Parameter[] parameters = method.getParameters();
        ParameterProcessor[] processors = new ParameterProcessor[parameters.length];

        for(int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            for(ParameterProcessor processor : parent.getProcessors()) {
                if(processor.applies(method, parameter)) {
                    processors[i] = processor;
                    break;
                }
            }
        }

        return new ProcessorMappings(method, processors);
    }

    public static ProcessorMappings of(Constructor<?> constructor, JWebServer parent) {
        Parameter[] parameters = constructor.getParameters();
        ParameterProcessor[] processors = new ParameterProcessor[parameters.length];

        for(int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];

            for(ParameterProcessor processor : parent.getProcessors()) {
                if(processor.applies(constructor, parameter)) {
                    processors[i] = processor;
                    break;
                }
            }
        }

        return new ProcessorMappings(constructor, processors);
    }

    /**
     * The target this mapping is for, methods or constructors
     */
    private final Executable target;
    /**
     * These processors will be applied to the targets parameters. <br>
     * processors.length = target.getParameters().length
     * Processor with index 0 will be applied to parameter 0 of the target and so on...
     */
    private final ParameterProcessor[] processors;

    public ProcessorMappings(Executable target, ParameterProcessor[] processors) {
        this.target = target;
        this.processors = processors;
    }

    public Executable getTarget() {
        return target;
    }

    public ParameterProcessor[] getProcessors() {
        return processors;
    }
}
