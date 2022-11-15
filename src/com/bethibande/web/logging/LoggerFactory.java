package com.bethibande.web.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LoggerFactory {

    public static Logger createLogger() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new StandardHandler());

        return logger;
    }

    public static void setLogStyle(LogStyle style, Logger logger) {
        ((StandardHandler)logger.getHandlers()[0]).setLogStyle(style);
    }

    public enum LogStyle {
        FANCY,
        SIMPLE
    }

    private static class StandardHandler extends Handler {

        private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMM yyyy mm:HH").toFormatter();
        private static final String STRING_FORMAT = "%s [%s] %s %s" + ConsoleColors.RESET;

        private Function<Level, String> styleProvider;
        private String padding = "%30s";

        public StandardHandler() {
            this.styleProvider = this::styleSimple;
        }

        public void setLogStyle(LogStyle style) {
            switch (style) {
                case FANCY -> this.styleProvider = this::styleFancy;
                case SIMPLE -> this.styleProvider = this::styleSimple;
            }
        }

        public String styleSimple(Level level) {
            String levelStr = level.getName();

            if(level == Level.INFO) {
                return ConsoleColors.BLUE + ConsoleColors.BOLD + levelStr + "   " + ConsoleColors.RESET;
            }
            if(level == Level.FINE) {
                return ConsoleColors.BLUE + ConsoleColors.BOLD + "INFO   " + ConsoleColors.RESET;
            }
            if(level == Level.FINER) {
                return ConsoleColors.MAGENTA + ConsoleColors.BOLD + "INFO   " + ConsoleColors.RESET;
            }
            if(level == Level.WARNING) {
                return ConsoleColors.ORANGE + ConsoleColors.BOLD + levelStr + ConsoleColors.RESET;
            }
            if(level == Level.SEVERE) {
                return ConsoleColors.RED + ConsoleColors.BOLD + levelStr + " " + ConsoleColors.RESET + ConsoleColors.RED;
            }
            if(level == Level.CONFIG) {
                return ConsoleColors.GREEN + ConsoleColors.BOLD + levelStr + " " + ConsoleColors.RESET;
            }
            if(level == Level.FINEST) {
                return ConsoleColors.MAGENTA + ConsoleColors.BOLD + levelStr + " " + ConsoleColors.RESET;
            }

            return null;
        }

        private String styleFancy(Level level) {
            String levelStr = level.getName();

            if(level == Level.INFO) { // TODO: add fine, finer and finest here
                return ConsoleColors.BACKGROUND_BLUE + ConsoleColors.BLACK + " " + levelStr + "    " + ConsoleColors.RESET;
            }
            if(level == Level.WARNING) {
                return ConsoleColors.BACKGROUND_ORANGE + ConsoleColors.BLACK + " " + levelStr + " " + ConsoleColors.RESET;
            }
            if(level == Level.SEVERE) {
                return ConsoleColors.BACKGROUND_RED + ConsoleColors.BLACK + " " + levelStr + "  " + ConsoleColors.RESET + ConsoleColors.RED;
            }
            if(level == Level.CONFIG) {
                return ConsoleColors.BACKGROUND_GREEN + ConsoleColors.BLACK + " " + levelStr + "  " + ConsoleColors.RESET;
            }
            if(level == Level.FINEST) {
                return ConsoleColors.BACKGROUND_MAGENTA + ConsoleColors.BLACK + " " + levelStr + "  " + ConsoleColors.RESET;
            }
            return null;
        }

        public void setPadding(int length) {
            this.padding = "%" + length + "s";
        }

        private String pad(String str) {
            return String.format(padding, str).replaceFirst("0+", " ");
        }

        @Override
        public void publish(LogRecord record) {
            Level level = record.getLevel();
            String levelStr = styleProvider.apply(level);
            LocalDateTime date = LocalDateTime.ofInstant(record.getInstant(), TimeZone.getDefault().toZoneId());

            System.out.println(String.format(STRING_FORMAT, FORMATTER.format(date), pad(Thread.currentThread().getName()), levelStr, record.getMessage()));
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void close() throws SecurityException { }
    }

}
