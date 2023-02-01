package com.bethibande.web.processors.client;

import com.bethibande.web.annotations.HeaderValue;
import com.bethibande.web.context.IContext;
import com.bethibande.web.types.Predicates;
import com.bethibande.web.types.Request;

import java.lang.reflect.Parameter;

public class ClientHeaderProcessor extends ClientParameterProcessor {

    public ClientHeaderProcessor() {
        super(Predicates.forAnnotation(HeaderValue.class), null);
        function(this::process);
    }

    private void process(final IContext context, final Parameter parameter, final Object value) {
        final Request request = context.request();
        final HeaderValue annotation = parameter.getAnnotation(HeaderValue.class);

        request.headers().add(annotation.header(), value.toString());
    }

}
