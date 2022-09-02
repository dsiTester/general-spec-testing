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
     * @return the substitution map
     */
    public Map<String, ?> getSubstitutionMap() { // definition of b
        return substitutionMap;
    }

    public CommandLine(final String executable) { // called from test
        this.isFile=false;
        this.executable=toCleanExecutable(executable); // call to a
    }

    public String[] toStrings() { // called from DefaultExecutor.execute()
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable(); // calls b
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
    }

    public String getExecutable() { // called from above
        // Expand the executable and replace '/' and '\\' with the platform
        // specific file separator char. This is safe here since we know
        // that this is a platform specific command.
        return StringUtils.fixFileSeparatorChar(expandArgument(executable)); // calls b
    }

    private String expandArgument(final String argument) { // called from above
        final StringBuffer stringBuffer = StringUtils.stringSubstitution(argument, this.getSubstitutionMap(), true); // call to b
        return stringBuffer.toString();
    }
}

public class DefaultExecutorTest {
    @Test
    public void testAddEnvironmentVariables() throws Exception {
        final Map<String, String> myEnvVars = new HashMap<>(EnvironmentUtils.getProcEnvironment());
        myEnvVars.put("NEW_VAR","NEW_VAL");
        exec.execute(new CommandLine(environmentSript), myEnvVars); // calls a
        final String environment = baos.toString().trim(); // calls b; throws exception
        assertTrue("Expecting NEW_VAR in "+environment,environment.indexOf("NEW_VAR") >= 0);
        assertTrue("Expecting NEW_VAL in "+environment,environment.indexOf("NEW_VAL") >= 0);
    }

    /**
     * Try to start an non-existing application which should result
     * in an exception.
     */
    @Test(expected = IOException.class)
    public void testExecuteNonExistingApplicationWithWatchDog() throws Exception {
        final CommandLine cl = new CommandLine(nonExistingTestScript); // calls a
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));

        executor.execute(cl);   // calls b
    }

}
