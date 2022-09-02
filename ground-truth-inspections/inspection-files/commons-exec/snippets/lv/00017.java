public class CommandLine {
    /**
     * Add multiple arguments. Handles parsing of quotes and whitespace.
     * Please note that the parsing can have undesired side-effects therefore
     * it is recommended to build the command line incrementally.
     *
     * @param addArguments An string containing multiple arguments.
     * @param handleQuoting Add the argument with/without handling quoting
     * @return The command line itself
     */
    public CommandLine addArguments(final String addArguments, final boolean handleQuoting) { // called from test
        if (addArguments != null) {
            final String[] argumentsArray = translateCommandline(addArguments);
            addArguments(argumentsArray, handleQuoting); // call to a
        }

        return this;
    }

    /**
     * Returns the executable.
     *
     * @return The executable
     */
    public String getExecutable() { // definition of b
        // Expand the executable and replace '/' and '\\' with the platform
        // specific file separator char. This is safe here since we know
        // that this is a platform specific command.
        return StringUtils.fixFileSeparatorChar(expandArgument(executable));
    }

    public String[] toStrings() { // called from test
        final String[] result = new String[arguments.size() + 1]; // precisely speaking, the test would fail if method-a isn't called at this point.
        result[0] = this.getExecutable(); // call to b
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
    }

}

public class CommandLineTest {
    @Test
     public void testComplexAddArguments2() {
         final CommandLine cmdl = new CommandLine("runMemorySud.cmd");
         // NOTE: call method-b before method-a by uncommenting the following line
         // System.out.println(cmdl.getExecutable());
         cmdl.addArguments("10 30 -XX:+UseParallelGC '\"-XX:ParallelGCThreads=2\"'", false); // call to a
         assertArrayEquals(new String[]{"runMemorySud.cmd", "10", "30", "-XX:+UseParallelGC", "\"-XX:ParallelGCThreads=2\""}, cmdl.toStrings()); // calls b; assertion fails here
     }

}
