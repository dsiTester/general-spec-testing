public class Watchdog implements Runnable {

    public synchronized void start() { // definition of b
        stopped = false;
        final Thread t = new Thread(this, "WATCHDOG");
        t.setDaemon(true);
        t.start();              // this runs a thread with Watchdog.run()
    }

    public synchronized void stop() { // definition of b
        stopped = true;
        notifyAll();
    }

    @Override
    public void run() {         // called when a starts the thread
        final long startTimeMillis = System.currentTimeMillis();
        boolean isWaiting;
        synchronized (this) {
            long timeLeftMillis = timeoutMillis - (System.currentTimeMillis() - startTimeMillis);
            isWaiting = timeLeftMillis > 0;
            while (!stopped && isWaiting) {
                try {
                    wait(timeLeftMillis);
                } catch (final InterruptedException e) {
                }
                timeLeftMillis = timeoutMillis - (System.currentTimeMillis() - startTimeMillis);
                isWaiting = timeLeftMillis > 0;
            }
        }

        // notify the listeners outside of the synchronized block (see EXEC-60)
        if (!isWaiting) {
            fireTimeoutOccured();
        }
    }

}

public class ExecuteWatchdog implements TimeoutObserver {

    public synchronized void start(final Process processToMonitor) { // called from DefaultExecutor.executeInternal()
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
            watchdog.start();   // call to a
        }
    }

    public synchronized void stop() { // called from DefaultExecutor.executeInternal()
        if (hasWatchdog) {
            watchdog.stop();    // call to b
        }
        watch = false;
        process = null;
    }

}


public class DefaultExecutor implements Executor {
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from thread created and started by DefaultExecutor.execute(CommandLine, Map, ExecuteResultHandler)

        final Process process;
        exceptionCaught = null;

        try {
            process = this.launch(command, environment, dir);
        }
        catch(final IOException e) {
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        try {
            streams.setProcessInputStream(process.getOutputStream());
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        streams.start();

        try {
            ...

            // associate the watchdog with the newly created process
            if (watchdog != null) {
                watchdog.start(process); // calls a
            }

            ...
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
                watchdog.stop(); // calls b
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
            return exitValue;
        } finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().remove(process);
            }
        }
    }
}


public class Exec41Test {
    @Test
    public void testExec41WithStreams() throws Exception { // validated test

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

        ...
        assertTrue("The process was killed by the watchdog", watchdog.killedProcess()); // assertion failed here
        assertTrue("Skipping the Thread.join() did not work", duration < 9000);
    }

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
