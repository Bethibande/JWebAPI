package com.bethibande.web.types;

import java.util.concurrent.ExecutorService;

public interface HasExecutor {

    ExecutorService getExecutor();
    void setExecutor(final ExecutorService executor);

}
