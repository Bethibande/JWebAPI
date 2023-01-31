package com.bethibande.web.logging;

@SuppressWarnings("unused")
public class ConsoleColors {

    public static final String BLACK = "\u001b[30m";
    public static final String RED = "\u001b[31m";
    public static final String GREEN = "\u001b[32m";
    public static final String YELLOW = "\u001b[33m";
    public static final String BLUE = "\u001b[34m";
    public static final String MAGENTA = "\u001b[35m";
    public static final String CYAN = "\u001b[36m";
    public static final String WHITE = "\u001b[37m";

    public static final String BACKGROUND_BLACK = "\u001b[40m";
     public static final String BACKGROUND_RED = "\u001b[41m";
     public static final String BACKGROUND_GREEN = "\u001b[42m";
     public static final String BACKGROUND_YELLOW = "\u001b[43m";
     public static final String BACKGROUND_BLUE = "\u001b[44m";
     public static final String BACKGROUND_MAGENTA = "\u001b[45m";
     public static final String BACKGROUND_ORANGE = "\u001b[46m";
     public static final String BACKGROUND_WHITE = "\u001b[47m";

    public static final String RESET = "\u001b[0m";
    public static final String BOLD = "\u001b[1m";

    /**
     * Returns "color + str + {@link #RESET}"
     */
    public static String annotate(String str, String color) {
        return color + str + ConsoleColors.RESET;
    }

}
