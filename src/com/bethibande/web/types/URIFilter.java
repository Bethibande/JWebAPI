package com.bethibande.web.types;

public interface URIFilter {

    /**
     * Checks whether a handler should be executed, based on the uri supplied by the @URI annotation and the path of the request
     * @param annotated value of the @URI annotation
     * @param path path of the request
     * @return true if the handler should be executed
     * @see com.bethibande.web.annotations.URI
     * @see URIObject
     */
    boolean apply(String annotated, String path);

}
