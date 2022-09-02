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

    public CommandLine addArgument(final String argument) { // called from test
        return this.addArgument(argument, true);            // call to a
    }

    public String[] toStrings() { // indirectly called from DefaultExecutor.execute()
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable();
        System.arraycopy(getArguments(), 0, result, 1, result.length-1); // call to b
        return result;
    }

}

public class DefaultExecutorTest {
    @Test
    public void testExecuteWithArg() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        cl.addArgument("BAR");  // calls a
        final int exitValue = exec.execute(cl); // calls b

        assertEquals("FOO..BAR", baos.toString().trim()); // this assertion fails
        assertFalse(exec.isFailure(exitValue));
    }

}

public class Exec65Test extends AbstractExecTest {
    @Test(expected = ExecuteException.class, timeout = TEST_TIMEOUT)
    public void testExec65WitSleepUsingSleepCommandDirectly() throws Exception { // invalidated test

        if (!OS.isFamilyUnix()) {
            throw new ExecuteException(testNotSupportedForCurrentOperatingSystem(), 0);
        }
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(WATCHDOG_TIMEOUT);
        final DefaultExecutor executor = new DefaultExecutor();
        final CommandLine command = new CommandLine("sleep");
        command.addArgument("60"); // calls a
        executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
        executor.setWatchdog(watchdog);

        executor.execute(command); // calls b
    }

}

/*

SCRIPT CALLED BY VALIDATING TEST:

echo FOO.$TEST_ENV_VAR.$1
*/
