package com.bethibande.web.examples.test.annotations;

import com.bethibande.web.examples.test.permission.Access;
import com.bethibande.web.examples.test.permission.Permissions;

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
