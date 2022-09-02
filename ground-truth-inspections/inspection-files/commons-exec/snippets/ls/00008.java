public class CommandLine {
   /**
    * Add a single argument.
    *
    * @param argument The argument to add
    * @param handleQuoting Add the argument with/without handling quoting
    * @return The command line itself
    */
    public CommandLine addArgument(final String argument, final boolean handleQuoting) { // definition of a

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

    /**
     * Set the substitutionMap to expand variables in the
     * command line.
     *
     * @param substitutionMap the map
     */
    public void setSubstitutionMap(final Map<String, ?> substitutionMap) { // definition of b
        this.substitutionMap = substitutionMap;
    }

    public CommandLine addArgument(final String argument) { // called from test
        return this.addArgument(argument, true);            // call to a
    }
}

public class CommandLineTest {
    @Test
    public void testCopyConstructor()
    {
        final Map<String, String> map = new HashMap<>();
        map.put("bar", "bar");
        final CommandLine other = new CommandLine("test");
        other.addArgument("foo"); // calls a
        other.setSubstitutionMap(map); // call to b

        final CommandLine cmdl = new CommandLine(other);
        assertEquals(other.getExecutable(), cmdl.getExecutable());
        assertArrayEquals(other.getArguments(), cmdl.getArguments());
        assertEquals(other.isFile(), cmdl.isFile());
        assertEquals(other.getSubstitutionMap(), cmdl.getSubstitutionMap());

    }
}
