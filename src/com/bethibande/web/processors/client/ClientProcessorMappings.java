package com.bethibande.web.processors.client;

import com.bethibande.web.JWebClient;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public record ClientProcessorMappings(Executable target, ClientParameterProcessor[] processors) {

    public static ClientProcessorMappings of(final Executable executable, final JWebClient client) {
        final ClientParameterProcessor[] processors = new ClientParameterProcessor[executable.getParameters().length];
        final Parameter[] parameters = executable.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            final ClientParameterProcessor processor = client.getProcessors()
                    .stream()
                    .filter(p -> p.predicate().apply(parameter))
                    .findFirst()
                    .orElse(null);

            processors[i] = processor;
        }

        return new ClientProcessorMappings(executable, processors);
    }

}
