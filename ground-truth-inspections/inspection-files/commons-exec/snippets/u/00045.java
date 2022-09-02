public class CommandLine {
    /**
     * Returns the command line as an array of strings.
     *
     * @return The command line as an string array
     */
    public String[] toStrings() { // definition of a
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable();
        System.arraycopy(getArguments(), 0, result, 1, result.length-1); // call to b
        return result;
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

}

public class Java13CommandLauncher extends CommandLauncherImpl {
    @Override
    public Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException { // called from DefaultExecutor.execute()

        final String[] envVars = EnvironmentUtils.toStrings(env);

        return Runtime.getRuntime().exec(cmd.toStrings(), // call to a
                envVars, workingDir);
    }
}

public class DefaultExecutorTest {
    @Test
    public void testAddEnvironmentVariables() throws Exception {
        final Map<String, String> myEnvVars = new HashMap<>(EnvironmentUtils.getProcEnvironment());
        myEnvVars.put("NEW_VAR","NEW_VAL");
        exec.execute(new CommandLine(environmentSript), myEnvVars); // calls a
        final String environment = baos.toString().trim();
        assertTrue("Expecting NEW_VAR in "+environment,environment.indexOf("NEW_VAR") >= 0);
        assertTrue("Expecting NEW_VAL in "+environment,environment.indexOf("NEW_VAL") >= 0);
    }

}
