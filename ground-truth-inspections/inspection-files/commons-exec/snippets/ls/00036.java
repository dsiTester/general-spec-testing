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

    public CommandLine(final String executable) { // called from test
        this.isFile=false;
        this.executable=toCleanExecutable(executable); // call to a
    }

    public CommandLine addArguments(final String addArguments) { // called from test
        return this.addArguments(addArguments, true);            // call to a
    }

    public CommandLine addArguments(final String addArguments, final boolean handleQuoting) { // called from above
        if (addArguments != null) {
            final String[] argumentsArray = translateCommandline(addArguments);
            addArguments(argumentsArray, handleQuoting); // call to b
        }

        return this;
    }

    public CommandLine addArgument(final String argument, final boolean handleQuoting) { // called from b

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
    public void testAddArguments() {
        final CommandLine cmdl = new CommandLine("test"); // calls a
        cmdl.addArguments("foo bar");                     // calls b
        assertEquals("[test, foo, bar]", cmdl.toString());
        assertArrayEquals(new String[]{"test", "foo", "bar"}, cmdl.toStrings());
    }

}
