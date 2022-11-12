package com.bethibande.web.examples;

import com.bethibande.web.annotations.*;
import com.bethibande.web.examples.beans.TestBean;
import com.bethibande.web.response.RequestResponse;
import com.bethibande.web.sessions.Session;
import com.bethibande.web.types.RequestMethod;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class TestHandler {

    @URI("/count")
    public Object count(TestBean bean) {
        bean.increment();

        return new RequestResponse()
                .withStatusCode(202)
                .withContentData(new Message(bean.getNumber(), bean.getPath()));
    }

    /**
     * Stream files or other data in form of input streams
     */
    @URI("/file")
    public Object fileTest() {
        return RequestResponse.stream(getClass().getResourceAsStream("/test.html")/*, 9 (Stream Length, by default only uses InputStream.available()) */);
    }

    /**
     * Returned java objects will be sent as json data
     */
    @URI("/")
    public Object helloWorld() {
        return Message.MessageType.HELLO_WORLD.toMessage();
    }

    /**
     * Request that only allows post requests
     */
    @URI(value = "/postOnly", methods = RequestMethod.POST)
    public Object postOnlyTest() {
        return Message.MessageType.OK.toMessage();
    }

    /**
     * Receiving post data, @PostData annotation automatically turns json data into an Object
     */
    @URI(value = "/postMessage", methods = RequestMethod.POST)
    public Object postMessage(@PostData Message message) {
        return new Message(99, message.getMessage());
    }

    /**
     * Receiving post data using @JsonField annotation, used for accessing specific fields within the received json data
     */
    @URI(value = "/postMessage2", methods = RequestMethod.POST)
    public Object postMessage2(@JsonField("message") String message) {
        return new Message(99, message);
    }

    /**
     * The @CacheRequest annotation caches the result server side, if global is true the response will be cached globally, if false the response will be cached in the current user session
     */
    @URI("/cache")
    @CacheRequest(cacheTime = 10000L, global = true)
    public Object cacheTest() {
        return new Message(99, String.valueOf(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE)));
    }

    /**
     * The @CacheRequest annotation caches the result server side, if global is true the response will be cached globally, if false the response will be cached in the current user session
     */
    @URI("/localCache")
    @CacheRequest(cacheTime = 10000L)
    public Object localCacheTest() {
        return new Message(99, String.valueOf(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE)));
    }

    /**
     * Display session data
     */
    @URI("/test2")
    public Object test2(Session session) {
        return new Message(99, String.format(
                "Hello %s!\nYour Session was created at %s",
                session.getMeta().get("name"),
                new Date(session.getCreationDate())));
    }

    /**
     * URIs using regex
     */
    @URI(value = "/test/[a-zA-Z0-9\\s]{3,16}",type = URI.URIType.REGEX)
    public Object test(
            @Path String path,
            Session session
    ) {
        session.getMeta().set("name", path.split("/")[2]);

        return RequestResponse.build()
                .withStatusCode(202)
                .withCharset(StandardCharsets.UTF_8)
                .withHeader("Date", new Date())
                .withContentData(new Message(3, "Hello " + path.split("/")[2]));
    }

    /**
     * Easily create redirects
     */
    @URI("/redirect")
    public Object redirect() {
        return RequestResponse.build()
                .withStatusCode(301)
                .withLocation("/test");
    }

}
