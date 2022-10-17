package com.bethibande.web.annotations;

import com.bethibande.web.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface URI {

    String value();
    boolean strict() default true;
    RequestMethod[] methods() default RequestMethod.GET;

}
