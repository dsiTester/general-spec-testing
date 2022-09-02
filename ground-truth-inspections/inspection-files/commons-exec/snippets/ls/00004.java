public class CommandLine {

    /**
     * Add a single argument. Handles quoting.
     *
     * @param argument The argument to add
     * @return The command line itself
     * @throws IllegalArgumentException If argument contains both single and double quotes
     */
    public CommandLine addArgument(final String argument) { // definition of a
        return this.addArgument(argument, true);
    }

    /**
     * Set the substitutionMap to expand variables in the
     * command line.
     *
     * @param substitutionMap the map
     */
    public void setSubstitutionMap(final Map<String, ?> substitutionMap) { // definition of b
        this.substitutionMap = substitutionMap;
    }

   public CommandLine addArgument(final String argument, final boolean handleQuoting) { // called from a

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
    public void testCopyConstructor()
    {
        final Map<String, String> map = new HashMap<>();
        map.put("bar", "bar");
        final CommandLine other = new CommandLine("test");
        other.addArgument("foo"); // call to a
        other.setSubstitutionMap(map); // call to b

        final CommandLine cmdl = new CommandLine(other);
        assertEquals(other.getExecutable(), cmdl.getExecutable());
        assertArrayEquals(other.getArguments(), cmdl.getArguments());
        assertEquals(other.isFile(), cmdl.isFile());
        assertEquals(other.getSubstitutionMap(), cmdl.getSubstitutionMap());

    }
}
