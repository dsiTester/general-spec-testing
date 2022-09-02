public class CommandLine {
    /**
     * Add multiple arguments.
     *
     * @param addArguments An array of arguments
     * @param handleQuoting Add the argument with/without handling quoting
     * @return The command line itself
     */
    public CommandLine addArguments(final String[] addArguments, final boolean handleQuoting) { // definition of a
        if (addArguments != null) {
            for (final String addArgument : addArguments) {
                addArgument(addArgument, handleQuoting);
            }
        }

        return this;
    }

    /**
     * Returns the expanded and quoted command line arguments.
     *
     * @return The quoted arguments
     */
    public String[] getArguments() { // definition of b

        Argument currArgument;
        String expandedArgument;
        final String[] result = new String[arguments.size()];

        for (int i=0; i<result.length; i++) {
            currArgument = arguments.get(i);
            expandedArgument = expandArgument(currArgument.getValue());
            result[i] = currArgument.isHandleQuoting() ? StringUtils.quoteArgument(expandedArgument) : expandedArgument;
        }

        return result;
    }

    /**
     * Returns the command line as an array of strings.
     *
     * @return The command line as an string array
     */
    public String[] toStrings() {
        final String[] result = new String[arguments.size() + 1]; // precisely speaking, the test would fail if method-a isn't called at this point.
        result[0] = this.getExecutable();
        System.arraycopy(getArguments(), 0, result, 1, result.length-1); // call to b
        return result;
    }

   public CommandLine addArgument(final String argument, final boolean handleQuoting) { // indirectly called from a

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

       arguments.add(new Argument(argument, handleQuoting)); // modifies CommandLine.arguments
       return this;
   }
}

public class CommandLineTest {
    /**
     * Create a command line with pre-quoted strings to test SANDBOX-192,
     * e.g. "runMemorySud.cmd", "10", "30", "-XX:+UseParallelGC", "\"-XX:ParallelGCThreads=2\""
     */
    @Test
    public void testComplexAddArguments1() {
        final CommandLine cmdl = new CommandLine("runMemorySud.cmd");
        cmdl.addArguments(new String[] {"10", "30", "-XX:+UseParallelGC", "\"-XX:ParallelGCThreads=2\""}, false); // call to a
        assertArrayEquals(new String[]{"runMemorySud.cmd", "10", "30", "-XX:+UseParallelGC", "\"-XX:ParallelGCThreads=2\""}, cmdl.toStrings());
    }

}
