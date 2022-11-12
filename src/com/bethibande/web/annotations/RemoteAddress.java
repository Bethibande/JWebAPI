package com.bethibande.web.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on parameters, parameters annotated with this, must have the type INetSocketAddress.
 * This annotation will fill your parameter with the clients remote socket address
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RemoteAddress {
}
