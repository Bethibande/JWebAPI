package com.bethibande.web.examples.chat.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthRequired {

    /**
     * Simple auth checks if the given device/session owns any name
     */
    boolean simple() default false;

}
