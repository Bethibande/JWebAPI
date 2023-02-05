package com.bethibande.web.repository;

import com.bethibande.web.JWebClient;

import java.net.URL;

public interface Repository {

    JWebClient getOwner();

    /**
     * Returns the base-url of the repository, if value has never been set, returns the base-url of the owner
     */
    URL getBaseUrl();

    /**
     * Set the base-url of the repository, must be an absolute url, specifying a protocol/scheme like "http://127.0.0.1:345"
     */
    void setBaseUrl(final String url);

}
