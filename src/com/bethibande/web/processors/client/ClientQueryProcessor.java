package com.bethibande.web.processors.client;

import com.bethibande.web.annotations.QueryField;
import com.bethibande.web.context.IContext;
import com.bethibande.web.types.Predicates;
import com.bethibande.web.types.Request;

import java.lang.reflect.Parameter;
import java.net.URI;
import java.net.URLEncoder;

public class ClientQueryProcessor extends ClientParameterProcessor {

    public ClientQueryProcessor() {
        super(Predicates.forAnnotation(QueryField.class), null);
        function(this::process);
    }

    private void process(final IContext context, final Parameter parameter, final Object value) {
        final Request request = context.request();
        final QueryField annotation = parameter.getAnnotation(QueryField.class);

        final URI uri = request.uri();
        if(uri == null) throw new RuntimeException("No uri found when setting query parameters.");

        final String text = String.format(
                "%s=%s",
                annotation.value(),
                URLEncoder.encode(value.toString(), context.api().getCharset())
        );

        request.setUri(URI.create(uri + (uri.getRawQuery() == null ? "?": "&") + text));
    }

}
