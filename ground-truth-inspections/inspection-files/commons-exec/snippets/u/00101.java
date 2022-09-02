public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine,
     *      java.util.Map, org.apache.commons.exec.ExecuteResultHandler)
     */
    @Override
    public void execute(final CommandLine command, final Map<String, String> environment,
            final ExecuteResultHandler handler) throws ExecuteException, IOException { // definition of a

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        if (watchdog != null) {
            watchdog.setProcessNotStarted();
        }

        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                int exitValue = Executor.INVALID_EXITVALUE;
                try {
                    exitValue = executeInternal(command, environment, workingDirectory, streamHandler);
                    handler.onProcessComplete(exitValue);
                } catch (final ExecuteException e) {
                    handler.onProcessFailed(e);
                } catch (final Exception e) {
                    handler.onProcessFailed(new ExecuteException("Execution failed", exitValue, e));
                }
            }
        };

        this.executorThread = createThread(runnable, "Exec Default Executor");
        getExecutorThread().start();
    }

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // definition of b

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

    @Override
    public void execute(final CommandLine command, final ExecuteResultHandler handler)
            throws ExecuteException, IOException { // called from test
        execute(command, null, handler);           // call to a
    }

}

public interface Executor {
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
        exec.execute(cl, resultHandler); // calls a
        resultHandler.waitFor(2000);
        assertTrue(resultHandler.hasResult()); // assertion fails here
        assertNull(resultHandler.getException());
        assertFalse(exec.isFailure(resultHandler.getExitValue())); // call to b
        assertEquals("FOO..", baos.toString().trim());
    }
}
