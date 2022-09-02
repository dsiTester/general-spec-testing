public class CommandLine {
    /**
     * Cleans the executable string. The argument is trimmed and '/' and '\\' are
     * replaced with the platform specific file separator char
     *
     * @param dirtyExecutable the executable
     * @return the platform-specific executable string
     */
    private String toCleanExecutable(final String dirtyExecutable) { // definition of a
        if (dirtyExecutable == null) {
            throw new IllegalArgumentException("Executable can not be null");
        }
        if (dirtyExecutable.trim().isEmpty()) {
            throw new IllegalArgumentException("Executable can not be empty");
        }
        return StringUtils.fixFileSeparatorChar(dirtyExecutable);
    }

    /**
     * Add multiple arguments. Handles parsing of quotes and whitespace.
     *
     * @param addArguments An array of arguments
     * @return The command line itself
     */
    public CommandLine addArguments(final String[] addArguments) { // b
        return this.addArguments(addArguments, true);
    }

    public CommandLine(final String executable) { // called from test
        this.isFile=false;
        this.executable=toCleanExecutable(executable); // call to a
    }

    public CommandLine addArgument(final String argument, final boolean handleQuoting) { // indirectly called from b

       if (argument == null)
       {
           return this;
       }

       // check if we can really quote the argument - if not throw an
       // IllegalArgumentException
       if (handleQuoting)
       {
           StringUtils.quoteArgument(argument);
       }

       arguments.add(new Argument(argument, handleQuoting));
       return this;
   }

}

public class CommandLineTest {
    @Test
    public void testAddArgumentsArrayNull() {
        final CommandLine cmdl = new CommandLine("test"); // calls a
        cmdl.addArguments((String[]) null); // call to b
        assertEquals("[test]", cmdl.toString());
        assertArrayEquals(new String[]{"test"}, cmdl.toStrings());
    }
}
