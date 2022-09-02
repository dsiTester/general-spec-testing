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
     * Returns the command line as an array of strings.
     *
     * @return The command line as an string array
     */
    public String[] toStrings() { // definition of b
        final String[] result = new String[arguments.size() + 1]; // critical point by which method-a needs to be called; otherwise the size of result will differ
        result[0] = this.getExecutable();
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
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

public class Java13CommandLauncher extends CommandLauncherImpl {
    @Override
    public Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException { // called indirectly from DefaultExecutor.execute()

        final String[] envVars = EnvironmentUtils.toStrings(env);

        return Runtime.getRuntime().exec(cmd.toStrings(), // call to b
                envVars, workingDir);
    }
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteWithArg() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        cl.addArgument("BAR");  // call to a
        final int exitValue = exec.execute(cl); // calls b

        assertEquals("FOO..BAR", baos.toString().trim()); // assertion failed
        assertFalse(exec.isFailure(exitValue));
    }

    @Test
    public void testExecuteWithComplexArguments() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(printArgsScript);
        cl.addArgument("gdal_translate"); // call to a
        cl.addArgument("HDF5:\"/home/kk/grass/data/4404.he5\"://HDFEOS/GRIDS/OMI_Column_Amount_O3/Data_Fields/ColumnAmountO3/home/kk/4.tif", false);
        final DefaultExecutor executor = new DefaultExecutor();
        final int exitValue = executor.execute(cl); // calls b
        assertFalse(exec.isFailure(exitValue));
     }

}

public class Exec57Test extends AbstractExecTest {
    @Test(timeout = TEST_TIMEOUT)
    public void testExecutionOfDetachedProcess() throws IOException { // unknown test

        if (!OS.isFamilyUnix()) {
            testNotSupportedForCurrentOperatingSystem();
            return;
        }

        final CommandLine cmdLine = new CommandLine("sh").addArgument("-c").addArgument("./src/test/scripts/issues/exec-57-detached.sh", false); // call to a; NullPointerException here
        final DefaultExecutor executor = new DefaultExecutor();
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(System.out, System.err);

        executor.setStreamHandler(pumpStreamHandler);

        executor.execute(cmdLine); // calls b
    }

}

/*

SCRIPT CALLED BY VALIDATING TEST:

echo FOO.$TEST_ENV_VAR.$1
*/

/*
SCRIPT CALLED BY INVALIDATING TEST:

while [ $# -gt 0 ]
do
    echo "$1"
    shift
done
*/
