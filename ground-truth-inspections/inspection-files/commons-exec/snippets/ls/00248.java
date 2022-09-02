public class ExecuteWatchdog implements TimeoutObserver {
    /**
     * Watches the given process and terminates it, if it runs for too long. All
     * information from the previous run are reset.
     *
     * @param processToMonitor
     *            the process to monitor. It cannot be {@code null}
     * @throws IllegalStateException
     *             if a process is still being monitored.
     */
    public synchronized void start(final Process processToMonitor) { // definition of a
        if (processToMonitor == null) {
            throw new NullPointerException("process is null.");
        }
        if (this.process != null) {
            throw new IllegalStateException("Already running.");
        }
        this.caught = null;
        this.killedProcess = false;
        this.watch = true;
        this.process = processToMonitor;
        this.processStarted = true;
        this.notifyAll();
        if (this.hasWatchdog) {
            watchdog.start();
        }
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

public class Watchdog implements Runnable {
    public synchronized void start() { // called from a
        stopped = false;
        final Thread t = new Thread(this, "WATCHDOG"); // this thread will call method-b after the timeout happens
        t.setDaemon(true);
        t.start();
    }
}

public class DefaultExecutor implements Executor {

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from the thread created and run by DefaultExecutor.execute()

        final Process process;
        exceptionCaught = null;

        ...

        try {

            ...
            // associate the watchdog with the newly created process
            if (watchdog != null) {
                watchdog.start(process); // call to a
            }

            int exitValue = Executor.INVALID_EXITVALUE;

            try {
                exitValue = process.waitFor();
            } catch (final InterruptedException e) {
                process.destroy();
            }
            finally {
                // see http://bugs.sun.com/view_bug.do?bug_id=6420270
                // see https://issues.apache.org/jira/browse/EXEC-46
                // Process.waitFor should clear interrupt status when throwing InterruptedException
                // but we have to do that manually
                Thread.interrupted();
            }

            if (watchdog != null) {
                watchdog.stop();
            }

            ...
            if (watchdog != null) {
                try {
                    watchdog.checkException();
                } catch (final IOException e) {
                    throw e;
                } catch (final Exception e) {
                    // Java 1.5 does not support public IOException(String message, Throwable cause)
                    final IOException ioe = new IOException(e.getMessage());
                    ioe.initCause(e);
                    throw ioe;
                }
            }
            ...
        }
        ...
        finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().remove(process);
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
        exec.execute(cmdLine, handler); // calls a
        handler.waitFor();
        assertTrue("Process has exited", handler.hasResult());
        assertNotNull("Process was aborted", handler.getException());
        assertTrue("Watchdog should have killed the process", watchdog.killedProcess());
        assertFalse("Watchdog is no longer watching the process", watchdog.isWatching()); // call to b
    }

}
