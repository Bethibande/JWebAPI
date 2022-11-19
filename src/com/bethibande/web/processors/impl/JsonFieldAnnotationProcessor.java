package com.bethibande.web.processors.impl;

import com.bethibande.web.annotations.JsonField;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.AnnotationProcessor;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class JsonFieldAnnotationProcessor extends AnnotationProcessor<JsonField> {

    public JsonFieldAnnotationProcessor() {
        super(JsonField.class, true);
    }


    @Override
    public Object accept(ServerContext context, JsonField annotation, Executable executable, Parameter parameter) {
        final WebRequest request = context.request();
        Gson gson = request.getServer().getGson();

        if(!context.metadata().hasMeta("JsonData")) {
            String json = IOUtils.readString(
                    request.getExchange().getRequestBody(),
                    request.getContentLength(),
                    context.metadata().getCharset(),
                    context.metadata().getBufferSize()
            );

            context.metadata().set(
                    "JsonData",
                    gson.fromJson(json, JsonObject.class)
            );
        }

        JsonObject jsonObject = context.metadata().getAsType("JsonData", JsonObject.class);

        if(!jsonObject.has(annotation.value())) throw new RuntimeException("There is no such field '" + annotation.value() + "'!");

        return gson.fromJson(jsonObject.get(annotation.value()), parameter.getType());
    }
}
