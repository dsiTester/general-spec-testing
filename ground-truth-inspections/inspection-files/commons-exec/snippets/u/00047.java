public class CommandLine {
    /**
     * Returns the command line as an array of strings.
     *
     * @return The command line as an string array
     */
    public String[] toStrings() { // definition of a
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable(); // calls b
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
    }

    public String getExecutable() { // called from a
        // Expand the executable and replace '/' and '\\' with the platform
        // specific file separator char. This is safe here since we know
        // that this is a platform specific command.
        return StringUtils.fixFileSeparatorChar(expandArgument(executable)); // calls b
    }

    /**
     * Expand variables in a command line argument.
     *
     * @param argument the argument
     * @return the expanded string
     */
    private String expandArgument(final String argument) { // called from above
        final StringBuffer stringBuffer = StringUtils.stringSubstitution(argument, this.getSubstitutionMap(), true); // call to b
        return stringBuffer.toString();
    }

    /**
     * @return the substitution map
     */
    public Map<String, ?> getSubstitutionMap() { // definition of b
        return substitutionMap;
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
