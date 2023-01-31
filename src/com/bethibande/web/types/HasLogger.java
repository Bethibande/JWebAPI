package com.bethibande.web.types;

import java.util.logging.Logger;

public interface HasLogger {

    void setLogger(final Logger logger);
    Logger getLogger();

}
