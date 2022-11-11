package com.bethibande.web.processors.impl;

import com.bethibande.web.annotations.PostData;
import com.bethibande.web.context.LocalServerContext;
import com.bethibande.web.context.ServerContext;
import com.bethibande.web.processors.AnnotationProcessor;
import com.bethibande.web.types.WebRequest;
import com.bethibande.web.util.IOUtils;
import com.google.gson.Gson;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

public class PostDataAnnotationProcessor extends AnnotationProcessor<PostData> {

    public PostDataAnnotationProcessor() {
        super(PostData.class);
    }

    @Override
    public Object getValue(WebRequest request, PostData annotation, Executable executable, Parameter parameter) {
        ServerContext context = LocalServerContext.getContext();
        if(context == null) throw new RuntimeException("Invoking annotation processor without a context?");

        String json = IOUtils.readString(
                request.getExchange().getRequestBody(),
                request.getContentLength(),
                request.getServer().getCharset(),
                request.getServer().getBufferSize()
        );

        if(json.isEmpty()) return null;

        return new Gson().fromJson(json, parameter.getType());
    }
}
