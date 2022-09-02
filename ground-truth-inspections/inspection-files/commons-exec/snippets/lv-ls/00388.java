public class Watchdog implements Runnable {

    public void addTimeoutObserver(final TimeoutObserver to) { // definition of a
        observers.addElement(to);
    }

    public synchronized void stop() { // definition of b
        stopped = true;
        notifyAll();
    }
}

public class ExecuteWatchdog implements TimeoutObserver {

    public ExecuteWatchdog(final long timeout) {
        this.killedProcess = false;
        this.watch = false;
        this.hasWatchdog = timeout != INFINITE_TIMEOUT;
        this.processStarted = false;
        if (this.hasWatchdog) {
            this.watchdog = new Watchdog(timeout);
            this.watchdog.addTimeoutObserver(this); // call to a
        }
        else {
            this.watchdog = null;
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
                watchdog.start(process); // starts Watchdog.run()
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
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(2 * 1000); // calls a; allow process no more than 2 seconds
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(System.out, System.err);
        // this method was part of the patch I reverted
        // pumpStreamHandler.setAlwaysWaitForStreamThreads(false);

        executor.setWatchdog(watchdog);
        executor.setStreamHandler(pumpStreamHandler);

        final long startTime = System.currentTimeMillis();

        try {
            executor.execute(cmdLine); // calls b
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
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout); // calls a
        executor.setWatchdog(watchdog);

        try {
            executor.execute(cl); // calls b
        } catch (final ExecuteException e) {
            assertFalse("Process should exit normally, not be killed by watchdog", watchdog.killedProcess());
            // If the Watchdog did not kill it, something else went wrong.
            throw e;
        }
    }

}
