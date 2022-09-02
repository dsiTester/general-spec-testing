public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine, java.util.Map)
     */
    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // only implementation of a

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // calls b

    }

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // only implementation of b

        if (this.exitValues == null) {
            return false;
        }
        if (this.exitValues.length == 0) {
            return this.launcher.isFailure(exitValue);
        }
        for (final int exitValue2 : this.exitValues) {
            if (exitValue2 == exitValue) {
                return false;
            }
        }
        return true;
    }

}

public interface Executor {
    /**
     * Methods for starting synchronous execution.
     *
     * @param command the command to execute
     * @param environment The environment for the new process. If null, the
     *          environment of the current process is used.
     * @return process exit value
     * @throws ExecuteException execution of subprocess failed or the
     *          subprocess returned a exit value indicating a failure
     *          {@link Executor#setExitValue(int)}.
     */
    int execute(CommandLine command, Map<String, String> environment)
        throws ExecuteException, IOException; // a

    /**
     * Checks whether {@code exitValue} signals a failure. If no
     * exit values are set than the default conventions of the OS is
     * used. e.g. most OS regard an exit code of '0' as successful
     * execution and everything else as failure.
     *
     * @param exitValue the exit value (return code) to be checked
     * @return {@code true} if {@code exitValue} signals a failure
     */
    boolean isFailure(final int exitValue); // b
}

public class DefaultExecutorTest {
    /**
     * Execute the test script and pass a environment containing
     * 'TEST_ENV_VAR'.
     */
    @Test
    public void testExecuteWithSingleEnvironmentVariable() throws Exception {
        final Map<String, String> env = new HashMap<>();
        env.put("TEST_ENV_VAR", "XYZ");

        final CommandLine cl = new CommandLine(testScript);

        final int exitValue = exec.execute(cl, env); // call to a

        assertEquals("FOO.XYZ.", baos.toString().trim()); // assertion fails here
        assertFalse(exec.isFailure(exitValue)); // call to b
    }

}
