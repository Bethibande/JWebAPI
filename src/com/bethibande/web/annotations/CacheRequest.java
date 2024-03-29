package com.bethibande.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Used to cache request responses on the server side
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheRequest {

    /**
     * Time the request will be cached for, the cached response will be discarded after this amount of time.
     * Default value is 0, this will cache the request for the configured max item lifetime of the request cache
     * @return time in ms, time unit can be changed using {@link #timeUnit()}
     */
    long cacheTime() default 0;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * If true, cached values will be used for everyone, no matter which device sends a request with the same uri, it will receive the same, cached response.<br>
     * If false, cached values will only be used for the initial requester, if a device sends the same request twice within a certain amount of time, the cached response will be sent.
     */
    boolean global() default false;

}
