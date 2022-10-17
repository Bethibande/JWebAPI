package com.bethibande.web.handlers.out;

import com.bethibande.web.WebRequest;

/**
 * Used to handle the return values of requests
 */
public interface OutputHandler<T> {

    Class<T> getType();

    void update(T value, WebRequest request);

}
