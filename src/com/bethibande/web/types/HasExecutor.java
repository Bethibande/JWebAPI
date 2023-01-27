package com.bethibande.web.types;

import java.util.concurrent.ThreadPoolExecutor;

public interface HasExecutor {

    ThreadPoolExecutor getExecutor();
    void setExecutor(final ThreadPoolExecutor executor);

}
