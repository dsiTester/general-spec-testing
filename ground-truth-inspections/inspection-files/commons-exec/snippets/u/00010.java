public class CommandLine {
    /**
     * Add multiple arguments. Handles parsing of quotes and whitespace.
     *
     * @param addArguments An array of arguments
     * @return The command line itself
     */
    public CommandLine addArguments(final String[] addArguments) { // definition of a
        return this.addArguments(addArguments, true);              // call to b
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
    public void testAddArgumentsArray() {
        final CommandLine cmdl = new CommandLine("test");
        cmdl.addArguments(new string[] {"foo", "bar"}); // call to a
        assertEquals("[test, foo, bar]", cmdl.toString()); // assertion fails here
        assertArrayEquals(new String[]{"test", "foo", "bar"}, cmdl.toStrings());
    }

}
