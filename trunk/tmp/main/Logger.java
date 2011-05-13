package main;

import java.io.PrintStream;

/**
 * handles loggging of warnings and errors
 * @author orensf
 */
public class Logger {
    public static enum Level {
        WARNING("WARNING: "), ERROR("ERROR: ");

        private String prefix;

        private Level(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix;
        }
    }

    private static Level myLevel;
    private static PrintStream myOut;

    public static void init(Level level, PrintStream out) {
        myLevel = level;
        myOut = out;
    }

    public static void log(Level level, String message) {
        if (level.compareTo(myLevel)>=0) {
            myOut.println(level.getPrefix() + message);
            myOut.flush();
        }
    }

}
