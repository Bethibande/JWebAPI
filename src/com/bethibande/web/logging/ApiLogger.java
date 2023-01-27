package com.bethibande.web.logging;

import com.bethibande.web.JWebServer;
import com.bethibande.web.types.HasExecutor;

import java.util.logging.Logger;

public class ApiLogger extends Logger {

    private LogStyle style = new BasicStyle();

    private final HasExecutor owner;

    public ApiLogger(final HasExecutor owner, final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
        this.owner = owner;

        setUseParentHandlers(false);
        addHandler(new LogHandler(this));
    }

    public HasExecutor getOwner() {
        return owner;
    }

    /**
     * Set the message formatter used to format log messages
     * @see #getStyle()
     */
    @SuppressWarnings("unused")
    public void setStyle(final LogStyle style) {
        this.style = style;
    }

    /**
     * Get the message formatter used to format log messages
     * @see #setStyle(LogStyle)
     */
    public LogStyle getStyle() {
        return style;
    }
}
