package com.bethibande.web.logging;

import java.time.LocalDateTime;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FancyStyle implements LogStyle {

    @Override
    public String format(final LogMessage message) {
        final LogRecord record = message.getRecord();
        final String level = getLevelStyle(record.getLevel());
        final LocalDateTime date = LocalDateTime.ofInstant(record.getInstant(), TimeZone.getDefault().toZoneId());
        return String.format(BasicStyle.STRING_FORMAT,
                             BasicStyle.FORMATTER.format(date),
                             String.format("%30s", message.getThread().getName()).replaceFirst("0+", " "),
                             level,
                             record.getMessage());
    }

    private String getLevelStyle(final Level level) {
        String levelStr = level.getName();

        if(level == Level.INFO) {
            return ConsoleColors.BACKGROUND_BLUE + ConsoleColors.BLACK + " " + levelStr + "    " + ConsoleColors.RESET;
        }
        if(level == Level.FINE) {
            return ConsoleColors.BACKGROUND_YELLOW + ConsoleColors.BLACK + " INFO    " + ConsoleColors.RESET;
        }
        if(level == Level.FINER) {
            return ConsoleColors.BACKGROUND_YELLOW + ConsoleColors.BLACK + " INFO    " + ConsoleColors.RESET;
        }
        if(level == Level.FINEST) {
            return ConsoleColors.BACKGROUND_MAGENTA + ConsoleColors.BLACK + " INFO    " + ConsoleColors.RESET;
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
        return null;
    }

}
