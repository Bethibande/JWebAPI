package com.bethibande.web.examples;

import com.bethibande.web.annotations.CachedRequest;
import com.bethibande.web.annotations.Path;
import com.bethibande.web.annotations.URI;
import com.bethibande.web.response.RequestResponse;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TestHandler {

    @URI(value = "/", type = URI.URIType.STRING) // 404 Not found entry, not working, needs something like a priority
    public Message notFound() {
        return Message.MessageType.NOT_FOUND.toMessage();
    }

    @URI("/")
    public Object helloWorld() {
        return Message.MessageType.HELLO_WORLD.toMessage();
    }

    @URI(value = "/test/[a-zA-Z0-9\\s]{3,16}",type = URI.URIType.REGEX)
    @CachedRequest(cacheTime = 60000L, global = true)
    public Object test(@Path String path) {
        return RequestResponse.build()
                .withStatusCode(202)
                .withCharset(StandardCharsets.UTF_8)
                .withHeader("Date", new Date())
                .withContentData(new Message(3, "Hello " + path.split("/")[2]));
    }

    @URI("/redirect")
    public Object redirect() {
        return RequestResponse.build()
                .withStatusCode(301)
                .withLocation("/test");
    }

}
