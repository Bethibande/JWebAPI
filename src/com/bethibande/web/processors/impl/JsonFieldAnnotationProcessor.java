package com.bethibande.web.processors.impl;

import com.bethibande.web.annotations.JsonField;
import com.bethibande.web.context.LocalServerContext;
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
        super(JsonField.class);
    }


    @Override
    public Object getValue(WebRequest request, JsonField annotation, Executable executable, Parameter parameter) {
        ServerContext context = LocalServerContext.getContext();
        if(context == null) throw new RuntimeException("Cannot process @JsonField annotation without context.");
        if(!context.metadata().hasMeta("JsonData")) {
            String json = IOUtils.readString(
                    request.getExchange().getRequestBody(),
                    request.getContentLength(),
                    context.metadata().getCharset(),
                    context.metadata().getBufferSize()
            );

            context.metadata().set(
                    "JsonData",
                    new Gson().fromJson(json, JsonObject.class)
            );
        }

        JsonObject jsonObject = context.metadata().getAsType("JsonData", JsonObject.class);

        if(!jsonObject.has(annotation.value())) throw new RuntimeException("There is no such field '" + annotation.value() + "'!");

        return new Gson().fromJson(jsonObject.get(annotation.value()), parameter.getType());
    }
}
