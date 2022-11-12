package com.bethibande.web.annotations;

import com.bethibande.web.types.RequestMethod;

import java.lang.annotation.*;

/**
 * Used to annotate methods, this tells the server when your methods should be invoked. <br>
 * Default method is {@link RequestMethod#GET}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface URI {

    enum URIType {
        /**
         * This will match uris using regex, using String.matches(String regex);
         */
        REGEX,
        /**
         * This will match uris using String.equalsIgnoreCase(String);
         */
        STRICT,
        /**
         * This will match uris using String.startsWith(String);
         */
        STRING;
    }

    String value();
    URIType type() default URIType.STRICT;
    RequestMethod[] methods() default RequestMethod.GET;

}
