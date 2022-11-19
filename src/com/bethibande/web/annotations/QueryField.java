package com.bethibande.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on parameters, fills parameters with the value of the corresponding query tag or null if it doesn't exist
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryField {


    /**
     * Query key to get <br>
     * in case of query = test=abc&test2=def <br>
     * a value of "test2" will return def
     */
    String value();

}
