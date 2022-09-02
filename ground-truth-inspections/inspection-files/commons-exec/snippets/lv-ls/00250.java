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
     * Stops the watcher. It will notify all threads possibly waiting on this
     * object.
     */
    public synchronized void stop() { // definition of b
        if (hasWatchdog) {
            watchdog.stop();
        }
        watch = false;
        process = null;
    }
}

public class DefaultExecutor implements Executor {

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from the thread created and run by above

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
                watchdog.stop(); // call to b
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

public class Exec41Test {
    /**
     *
     * When a process runs longer than allowed by a configured watchdog's
     * timeout, the watchdog tries to destroy it and then DefaultExecutor
     * tries to clean up by joining with all installed pump stream threads.
     * Problem is, that sometimes the native process doesn't die and thus
     * streams aren't closed and the stream threads do not complete.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExec41WithStreams() throws Exception {

        CommandLine cmdLine;
        ...
        final DefaultExecutor executor = new DefaultExecutor();
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(2 * 1000); // allow process no more than 2 seconds
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(System.out, System.err);
        // this method was part of the patch I reverted
        // pumpStreamHandler.setAlwaysWaitForStreamThreads(false);

        executor.setWatchdog(watchdog);
        executor.setStreamHandler(pumpStreamHandler);

        final long startTime = System.currentTimeMillis();

        try {
            executor.execute(cmdLine); // calls a and b
        } catch (final ExecuteException e) {
            // nothing to do
        }

        final long duration = System.currentTimeMillis() - startTime;

        System.out.println("Process completed in " + duration + " millis; below is its output");

        if (watchdog.killedProcess()) {
            System.out.println("Process timed out and was killed by watchdog.");
        }

        assertTrue("The process was killed by the watchdog", watchdog.killedProcess()); // assertion fails here
        assertTrue("Skipping the Thread.join() did not work", duration < 9000);
    }

}

public class DefaultExecutorTest {
    /**
     * [EXEC-68] Synchronously starts a short script with a Watchdog attached with an extremely large timeout. Checks
     * to see if the script terminated naturally or if it was killed by the Watchdog. Fail if killed by Watchdog.
     *
     * @throws Exception
     *             the test failed
     */
    @Test
    public void testExecuteWatchdogVeryLongTimeout() throws Exception { // invalidated test
        final long timeout = Long.MAX_VALUE;

        final CommandLine cl = new CommandLine(testScript);
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File("."));
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
        executor.setWatchdog(watchdog);

        try {
            executor.execute(cl); // calls a and b
        } catch (final ExecuteException e) {
            assertFalse("Process should exit normally, not be killed by watchdog", watchdog.killedProcess());
            // If the Watchdog did not kill it, something else went wrong.
            throw e;
        }
    }

}
