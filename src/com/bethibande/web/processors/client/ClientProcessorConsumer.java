package com.bethibande.web.processors.client;

import com.bethibande.web.context.IContext;

import java.lang.reflect.Parameter;

public interface ClientProcessorConsumer {

    void apply(final IContext context, final Parameter parameter, final Object value);

}
