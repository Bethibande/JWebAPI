package com.bethibande.web.processors.client;

import com.bethibande.web.JWebAPI;
import com.bethibande.web.annotations.PostData;
import com.bethibande.web.context.IContext;
import com.bethibande.web.response.InputStreamWrapper;
import com.bethibande.web.types.Predicates;
import com.bethibande.web.types.Request;
import com.bethibande.web.writers.JsonWriter;
import com.bethibande.web.writers.StreamWriter;

import java.lang.reflect.Parameter;

public class PostDataProcessor extends ClientParameterProcessor {

    public PostDataProcessor() {
        super(Predicates.forAnnotation(PostData.class), null);
        function(this::process);
    }

    private void process(final IContext context, final Parameter parameter, final Object value) {
        final Request request = context.request();
        final JWebAPI api = context.api();

        if(value instanceof InputStreamWrapper wrapper) {
            request.setWriter(new StreamWriter(wrapper));
            return;
        }

        request.setWriter(new JsonWriter(api.getGson().toJson(value), api.getCharset()));
    }

}
