package com.bethibande.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
