public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine,
     *      org.apache.commons.exec.ExecuteResultHandler)
     */
    @Override
    public void execute(final CommandLine command, final ExecuteResultHandler handler)
            throws ExecuteException, IOException { // only implementation of a
        execute(command, null, handler);           // "calls" b
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
     * Methods for starting asynchronous execution. The child process inherits
     * all environment variables of the parent process. Result provided to
     * callback handler.
     *
     * @param command the command to execute
     * @param handler capture process termination and exit code
     * @throws ExecuteException execution of subprocess failed
     */
    void execute(CommandLine command, ExecuteResultHandler handler)
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
    @Test
    public void testExecuteAsync() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler); // call to a
        resultHandler.waitFor(2000);
        assertTrue(resultHandler.hasResult()); // assertion fails here
        assertNull(resultHandler.getException());
        assertFalse(exec.isFailure(resultHandler.getExitValue())); // call to b
        assertEquals("FOO..", baos.toString().trim());
    }

}
