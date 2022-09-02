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
     * Returns the command line as an array of strings.
     *
     * @return The command line as an string array
     */
    public String[] toStrings() { // called from test
        final String[] result = new String[arguments.size() + 1]; // precisely speaking, the test would fail if method-a isn't called at this point.
        result[0] = this.getExecutable(); // call to b
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
    }

    public CommandLine addArguments(final String addArguments, final boolean handleQuoting) { // called from test
        if (addArguments != null) {
            final String[] argumentsArray = translateCommandline(addArguments);
            addArguments(argumentsArray, handleQuoting); // call to a
        }

        return this;
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
    @Test
     public void testComplexAddArguments2() {
         final CommandLine cmdl = new CommandLine("runMemorySud.cmd");
         // NOTE: call method-b before method-a by uncommenting the following line
         // System.out.println(cmdl.getExecutable());
         cmdl.addArguments("10 30 -XX:+UseParallelGC '\"-XX:ParallelGCThreads=2\"'", false); // calls a
         assertArrayEquals(new String[]{"runMemorySud.cmd", "10", "30", "-XX:+UseParallelGC", "\"-XX:ParallelGCThreads=2\""}, cmdl.toStrings()); // call to b; assertion fails here
     }

}
