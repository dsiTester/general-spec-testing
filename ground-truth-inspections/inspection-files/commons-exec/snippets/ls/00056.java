public class DefaultExecuteResultHandler implements ExecuteResultHandler {
    /**
     * Causes the current thread to wait, if necessary, until the
     * process has terminated. This method returns immediately if
     * the process has already terminated. If the process has
     * not yet terminated, the calling thread will be blocked until the
     * process exits.
     *
     * @param timeoutMillis the maximum time to wait in milliseconds
     * @throws  InterruptedException if the current thread is
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    public void waitFor(final long timeoutMillis) throws InterruptedException { // definition of a

        final long untilMillis = System.currentTimeMillis() + timeoutMillis;

        while (!hasResult() && System.currentTimeMillis() < untilMillis) {
            Thread.sleep(SLEEP_TIME_MS);
        }
    }

    /**
     * @see org.apache.commons.exec.ExecuteResultHandler#onProcessFailed(org.apache.commons.exec.ExecuteException)
     */
    @Override
    public void onProcessFailed(final ExecuteException e) { // used implementation of b
        this.exitValue = e.getExitValue();
        this.exception = e;
        this.hasResult = true;
    }
}

public interface ExecuteResultHandler {

  /**
   * The asynchronous execution failed.
   *
   * @param e the {@code ExecuteException} containing the root cause
   */
    void onProcessFailed(ExecuteException e); // b
}

public class DefaultExecutor implements Executor {
    @Override
    public void execute(final CommandLine command, final Map<String, String> environment,
            final ExecuteResultHandler handler) throws ExecuteException, IOException { // called from DefaultExecutor.execute()

        ...
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
                    handler.onProcessFailed(e); // call to b
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
    public void testStdInHandling() throws Exception {
        // newline not needed; causes problems for VMS
        final ByteArrayInputStream bais = new ByteArrayInputStream("Foo".getBytes());
        final CommandLine cl = new CommandLine(this.stdinSript);
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(this.baos, System.err, bais);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        final Executor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.execute(cl, resultHandler);

        resultHandler.waitFor(WAITFOR_TIMEOUT); // call to a
        assertTrue("ResultHandler received a result", resultHandler.hasResult());

        assertFalse(exec.isFailure(resultHandler.getExitValue()));
        final String result = baos.toString();
        assertTrue("Result '" + result + "' should contain 'Hello Foo!'", result.indexOf("Hello Foo!") >= 0);
    }
}
