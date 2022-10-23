package com.bethibande.web.annotations;

import com.bethibande.web.types.RequestMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface URI {

    enum URIType {
        REGEX,
        STRICT,
        STRING;
    }

    String value();
    URIType type() default URIType.STRICT;
    RequestMethod[] methods() default RequestMethod.GET;

}
