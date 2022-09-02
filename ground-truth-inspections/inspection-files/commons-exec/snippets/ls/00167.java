public interface ExecuteResultHandler {

  /**
   * The asynchronous execution completed.
   *
   * @param exitValue the exit value of the sub-process
   */
    void onProcessComplete(int exitValue); // a

}

public class DefaultExecuteResultHandler implements ExecuteResultHandler {
    /**
     * @see org.apache.commons.exec.ExecuteResultHandler#onProcessComplete(int)
     */
    @Override
    public void onProcessComplete(final int exitValue) { // used implementation of a
        this.exitValue = exitValue;
        this.exception = null;
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
                    handler.onProcessComplete(exitValue); // call to a
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

}



public class DefaultExecutorTest {
    @Test
    public void testExecuteAsync() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler); // calls a
        resulthandler.waitFor(2000);
        assertTrue(resultHandler.hasResult());    // this would fail if method-a was not called
        assertNull(resultHandler.getException());
        assertFalse(exec.isFailure(resultHandler.getExitValue())); // call to b
        assertEquals("FOO..", baos.toString().trim());
    }
}
