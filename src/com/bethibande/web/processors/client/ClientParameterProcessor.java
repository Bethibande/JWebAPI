package com.bethibande.web.processors.client;

import java.lang.reflect.Parameter;
import java.util.function.Function;

public class ClientParameterProcessor {

    private final Function<Parameter, Boolean> predicate;
    private ClientProcessorConsumer function;

    public ClientParameterProcessor(final Function<Parameter, Boolean> predicate,
                                    final ClientProcessorConsumer function) {
        this.predicate = predicate;
        this.function = function;
    }

    public Function<Parameter, Boolean> predicate() {
        return predicate;
    }

    public ClientProcessorConsumer function() {
        return function;
    }

    public void function(final ClientProcessorConsumer function) {
        this.function = function;
    }
}
