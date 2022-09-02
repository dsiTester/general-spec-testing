public class DefaultExecuteResultHandler implements ExecuteResultHandler {
    /**
     * Causes the current thread to wait, if necessary, until the
     * process has terminated. This method returns immediately if
     * the process has already terminated. If the process has
     * not yet terminated, the calling thread will be blocked until the
     * process exits.
     *
     * @throws  InterruptedException if the current thread is
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    public void waitFor() throws InterruptedException {

        while (!hasResult()) {
            Thread.sleep(SLEEP_TIME_MS);
        }
    }

    /**
     * Get the {@code exception} causing the process execution to fail.
     *
     * @return Returns the exception.
     * @throws IllegalStateException if the process has not exited yet
     */
    public ExecuteException getException() {

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exception;
    }

}

public class Exec34Test {


    private final Executor exec = new DefaultExecutor();
    private final File testDir = new File("src/test/scripts");
    private final File pingScript = TestUtil.resolveScriptForOS(testDir + "/ping");

    /**
     * Some user waited for an asynchronous process using watchdog.isWatching() which
     * is now properly implemented  using {@code DefaultExecuteResultHandler}.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExec34_2() throws Exception {

        final CommandLine cmdLine = new CommandLine(pingScript);
        cmdLine.addArgument("10"); // sleep 10 seconds

        final ExecuteWatchdog watchdog = new ExecuteWatchdog(5000);
        final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
        exec.setWatchdog(watchdog);
        exec.execute(cmdLine, handler);
        handler.waitFor();      // call to a
        assertTrue("Process has exited", handler.hasResult()); // assertion fails here
        assertNotNull("Process was aborted", handler.getException()); // call to b
        assertTrue("Watchdog should have killed the process", watchdog.killedProcess());
        assertFalse("Watchdog is no longer watching the process", watchdog.isWatching());
    }
}
