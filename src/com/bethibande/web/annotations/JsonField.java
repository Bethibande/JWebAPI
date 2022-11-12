package com.bethibande.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate parameters, reads json post data and fills the parameters with the value of the specified field.<br>
 * Assuming your post data is {"username":"Max"} and you have a parameter @JsonField("username") String name, the name parameter will have a value of "Max".<br>
 * Not compatible with @JsonData
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonField {

    String value();

}
