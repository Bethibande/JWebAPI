package com.bethibande.web.examples.annotations;

import com.bethibande.web.examples.permission.Access;
import com.bethibande.web.examples.permission.Permissions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SecuredEntry {

    Permissions permission();
    Access access();

}
