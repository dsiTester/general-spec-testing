public interface ExecuteResultHandler {
  /**
   * The asynchronous execution failed.
   *
   * @param e the {@code ExecuteException} containing the root cause
   */
    void onProcessFailed(ExecuteException e); // a
}

public class DefaultExecuteResultHandler implements ExecuteResultHandler {

    /**
     * @see org.apache.commons.exec.ExecuteResultHandler#onProcessFailed(org.apache.commons.exec.ExecuteException)
     */
    @Override
    public void onProcessFailed(final ExecuteException e) { // used implementation of a
        this.exitValue = e.getExitValue();
        this.exception = e;
        this.hasResult = true;
    }

    /**
     * Get the {@code exitValue} of the process.
     *
     * @return Returns the exitValue.
     * @throws IllegalStateException if the process has not exited yet
     */
    public int getExitValue() { // definition of b

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exitValue;
    }
}

public class DefaultExecutor implements Executor {
    @Override
    public void execute(final CommandLine command, final Map<String, String> environment,
            final ExecuteResultHandler handler) throws ExecuteException, IOException { // called from DefaultExecutor.execute(CommandLine, ExecuteResultHandler)

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
                    handler.onProcessFailed(e); // call to a
                } catch (final Exception e) {
                    handler.onProcessFailed(new ExecuteException("Execution failed", exitValue, e));
                }
            }
        };

        this.executorThread = createThread(runnable, "Exec Default Executor");
        getExecutorThread().start(); // runs the thread that calls a
    }

}



public class DefaultExecutorTest {
    @Test
    public void testExecuteAsyncWithError() throws Exception {
        final CommandLine cl = new CommandLine(errorTestScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler); // calls a
        resultHandler.waitFor(2000);
        assertTrue(resultHandler.hasResult());
        assertTrue(exec.isFailure(resultHandler.getExitValue())); // call to b
        assertNotNull(resultHandler.getException());
        assertEquals("FOO..", baos.toString().trim());
    }

}
