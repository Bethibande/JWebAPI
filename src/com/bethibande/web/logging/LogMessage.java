package com.bethibande.web.logging;

import java.util.logging.LogRecord;

public class LogMessage {

    private final Thread thread;
    private final LogRecord record;
    private final ApiLogger logger;

    public LogMessage(Thread thread, LogRecord record, ApiLogger logger) {
        this.thread = thread;
        this.record = record;
        this.logger = logger;
    }

    /**
     * @return thread that created the log message
     */
    public Thread getThread() {
        return thread;
    }

    public LogRecord getRecord() {
        return record;
    }

    @SuppressWarnings("unused")
    public ApiLogger getLogger() {
        return logger;
    }
}
