public class CommandLine {
    /**
     * Add multiple arguments. Handles parsing of quotes and whitespace.
     * Please note that the parsing can have undesired side-effects therefore
     * it is recommended to build the command line incrementally.
     *
     * @param addArguments An string containing multiple arguments.
     * @return The command line itself
     */
    public CommandLine addArguments(final String addArguments) { // definition of a
        return this.addArguments(addArguments, true);
    }

    public CommandLine addArguments(final String addArguments, final boolean handleQuoting) { // called from a
        if (addArguments != null) {
            final String[] argumentsArray = translateCommandline(addArguments);
            addArguments(argumentsArray, handleQuoting); // call to b
        }

        return this;
    }

    /**
     * Add multiple arguments.
     *
     * @param addArguments An array of arguments
     * @param handleQuoting Add the argument with/without handling quoting
     * @return The command line itself
     */
    public CommandLine addArguments(final String[] addArguments, final boolean handleQuoting) { // definition of b
        if (addArguments != null) {
            for (final String addArgument : addArguments) {
                addArgument(addArgument, handleQuoting);
            }
        }

        return this;
    }

}

public class CommandLineTest {
    @Test
    public void testAddArgumentsWithQuotesAndSpaces() {
        final CommandLine cmdl = new CommandLine("test");
        cmdl.addArguments("'fo o' \"ba r\""); // call to a
        assertEquals("[test, \"fo o\", \"ba r\"]", cmdl.toString()); // assertion fails here
        assertArrayEquals(new String[]{"test", "\"fo o\"", "\"ba r\""}, cmdl
                .toStrings());
    }

}
