package com.bethibande.web.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class BasicStyle implements LogStyle {

    public static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd MMM yyyy HH:mm").toFormatter();
    public static final String STRING_FORMAT = "%s [%s] %s %s" + ConsoleColors.RESET;

    @Override
    public String format(final LogMessage message) {
        final LogRecord record = message.getRecord();
        final String level = getLevelStyle(record.getLevel());
        final LocalDateTime date = LocalDateTime.ofInstant(record.getInstant(), TimeZone.getDefault().toZoneId());
        return String.format(STRING_FORMAT,
                             FORMATTER.format(date),
                             String.format("%30s", message.getThread().getName()).replaceFirst("0+", " "),
                             level,
                             record.getMessage());
    }

    public String getLevelStyle(final Level level) {
        String levelStr = level.getName();

        if(level == Level.INFO) {
            return ConsoleColors.BLUE + ConsoleColors.BOLD + levelStr + "   " + ConsoleColors.RESET;
        }
        if(level == Level.FINE) {
            return ConsoleColors.YELLOW + ConsoleColors.BOLD + "INFO   " + ConsoleColors.RESET;
        }
        if(level == Level.FINER) {
            return ConsoleColors.YELLOW + ConsoleColors.BOLD + "INFO   " + ConsoleColors.RESET;
        }
        if(level == Level.FINEST) {
            return ConsoleColors.MAGENTA + ConsoleColors.BOLD + "INFO   " + ConsoleColors.RESET;
        }
        if(level == Level.WARNING) {
            return ConsoleColors.CYAN + ConsoleColors.BOLD + levelStr + ConsoleColors.RESET;
        }
        if(level == Level.SEVERE) {
            return ConsoleColors.RED + ConsoleColors.BOLD + levelStr + " " + ConsoleColors.RESET + ConsoleColors.RED;
        }
        if(level == Level.CONFIG) {
            return ConsoleColors.GREEN + ConsoleColors.BOLD + levelStr + " " + ConsoleColors.RESET;
        }

        return null;
    }

}
