package main;

/**
 *
 * @author orensf
 */
public enum Tool {
    MySentenceSplitter("oren"), JavaSentenceSplitter("java");

    private String argument;

    private Tool(String argument) {
        this.argument = argument;
    }

    public static Tool getTool(String argument) {
        for (Tool tool : Tool.values()) {
            if (tool.argument.equals(argument)) {
                return tool;
            }
        }
        // else, no tool was found...
        Logger.log(Logger.Level.ERROR,
                "could not find requested tool: " + argument);
        throw new IllegalArgumentException();
    }

}
