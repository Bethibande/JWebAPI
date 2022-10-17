package com.bethibande.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CachedRequest {

    /**
     * Time the request will be cached for, the cached response will be discarded after this amount of time.
     * @return time in ms
     */
    long cacheTime();

    /**
     * If true, cached values will be used for everyone, no matter which device sends a request with the same uri, it will receive the same, cached response.<br>
     * If false, cached values will only be used for the initial requester, if a device sends the same request twice within a certain amount of time, the cached response will be sent.
     */
    boolean global() default false;

}
