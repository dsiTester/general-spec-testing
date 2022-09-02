public interface Executor {
    /**
     * Set a custom the StreamHandler used for providing
     * input and retrieving the output. If you don't provide
     * a proper stream handler the executed process might block
     * when writing to stdout and/or stderr (see
     * {@link java.lang.Process Process}).
     *
     * @param streamHandler the stream handler
     */
    void setStreamHandler(ExecuteStreamHandler streamHandler); // a
}

public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // only implementation of a
        this.streamHandler = streamHandler;
    }

    /**
     * Get the worker thread being used for asynchronous execution.
     *
     * @return the worker thread
     */
    protected Thread getExecutorThread() { // definition of b
        return executorThread;
    }

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
                    handler.onProcessFailed(e);
                } catch (final Exception e) {
                    handler.onProcessFailed(new ExecuteException("Execution failed", exitValue, e));
                }
            }
        };

        this.executorThread = createThread(runnable, "Exec Default Executor"); // call to b
        getExecutorThread().start();
    }

}

public class DefaultExecutorTest {

    @Before
    public void setUp() throws Exception {

        // delete the marker file
        this.foreverOutputFile.getParentFile().mkdirs();
        if (this.foreverOutputFile.exists()) {
            this.foreverOutputFile.delete();
        }

        // prepare a ready to Executor
        this.baos = new ByteArrayOutputStream();
        this.exec.setStreamHandler(new PumpStreamHandler(baos, baos)); // call to a
    }

    @Test
    public void testExecuteAsync() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler); // calls b
        resultHandler.waitFor(2000);
        assertTrue(resultHandler.hasResult());
        assertNull(resultHandler.getException());
        assertFalse(exec.isFailure(resultHandler.getExitValue()));
        assertEquals("FOO..", baos.toString().trim());
    }
}
