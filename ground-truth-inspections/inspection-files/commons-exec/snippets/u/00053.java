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
     * Get the {@code exception} causing the process execution to fail.
     *
     * @return Returns the exception.
     * @throws IllegalStateException if the process has not exited yet
     */
    public ExecuteException getException() { // definition of b

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exception;
    }
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteAsyncWithTimelyUserTermination() throws Exception {
        final CommandLine cl = new CommandLine(foreverTestScript);
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);
        exec.setWatchdog(watchdog);
        final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
        exec.execute(cl, handler);
        // wait for script to run
        Thread.sleep(2000);
        assertTrue("Watchdog should watch the process", watchdog.isWatching());
        // terminate it manually using the watchdog
        watchdog.destroyProcess();
        // wait until the result of the process execution is propagated
        handler.waitFor(WAITFOR_TIMEOUT); // call to a
        assertTrue("Watchdog should have killed the process", watchdog.killedProcess());
        assertFalse("Watchdog is no longer watching the process", watchdog.isWatching());
        assertTrue("ResultHandler received a result", handler.hasResult()); // assertion fails here
        assertNotNull("ResultHandler received an exception as result", handler.getException()); // call to b
    }

}
