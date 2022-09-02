public class ExecuteWatchdog implements TimeoutObserver {
    /**
     * Indicates whether the last process run was killed.
     *
     * @return {@code true} if the process was killed
     *         {@code false}.
     */
    public synchronized boolean killedProcess() { // definition of a
        return killedProcess;
    }

    /**
     * Indicates whether or not the watchdog is still monitoring the process.
     *
     * @return {@code true} if the process is still running, otherwise
     *         {@code false}.
     */
    public synchronized boolean isWatching() { // definition of b
        ensureStarted();
        return watch;
    }

    private void ensureStarted() { // called from b
        while (!processStarted && caught == null) {
            try {
                this.wait();
            } catch (final InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

}

public class Exec34Test {
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
        handler.waitFor();
        assertTrue("Process has exited", handler.hasResult());
        assertNotNull("Process was aborted", handler.getException());
        assertTrue("Watchdog should have killed the process", watchdog.killedProcess()); // call to a
        assertFalse("Watchdog is no longer watching the process", watchdog.isWatching()); // call to b
    }

}
