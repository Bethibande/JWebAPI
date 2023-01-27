package com.bethibande.web.logging;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.HasExecutor;

import java.util.logging.Logger;

public class LoggerFactory {

    /**
     * Method used to create loggers used by JWebServer class
     * Log Levels,
     * Info - General info, like server start/stop
     * Config - "set" or "register" methods being called, also accounts for "with" method
     * Fine - Session Creation, Incoming requests [...]
     * Finer - Request timings
     * Finest - Cache Updates, methods being registered [...]
     */
    public static Logger createLogger(final HasExecutor owner) {
        return new ApiLogger(owner, null, null);
    }
}
