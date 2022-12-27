package com.bethibande.web.annotations;

import com.bethibande.web.types.RequestMethod;
import com.bethibande.web.types.URIFilter;

import java.lang.annotation.*;

/**
 * Used to annotate methods, this tells the server when your methods should be invoked. <br>
 * Default method is {@link RequestMethod#GET}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@SuppressWarnings("unused")
public @interface URI {

    enum URIType {
        /**
         * This will match uris using regex, using String.matches(String regex);
         */
        REGEX(((annotated, path) -> path.matches(annotated))),
        /**
         * This will match uris using String.equalsIgnoreCase(String);
         */
        STRICT(((annotated, path) -> path.equals(annotated))),
        /**
         * This will match uris using String.startsWith(String);
         */
        STRING(((annotated, path) -> path.startsWith(annotated)));

        public final URIFilter filter;

        URIType(URIFilter filter) {
            this.filter = filter;
        }
    }

    /**
     * Value must always start with "/" like "/api/get/yourResource"
     */
    String value();
    URIType type() default URIType.STRICT;
    RequestMethod[] methods() default RequestMethod.GET;
    int priority() default 0;

}
