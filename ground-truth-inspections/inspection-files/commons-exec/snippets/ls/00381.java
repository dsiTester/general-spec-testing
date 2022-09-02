public interface TimeoutObserver {

    /**
     * Called when the watchdog times out.
     *
     * @param w the watchdog that timed out.
     */
    void timeoutOccured(Watchdog w); // a
}

public class ExecuteWatchdog implements TimeoutObserver {
    /**
     * Called after watchdog has finished.
     */
    @Override
    public synchronized void timeoutOccured(final Watchdog w) { // only implementation of a
        try {
            try {
                // We must check if the process was not stopped
                // before being here
                if (process != null) {
                    process.exitValue();
                }
            } catch (final IllegalThreadStateException itse) {
                // the process is not terminated, if this is really
                // a timeout and not a manual stop then destroy it.
                if (watch) {
                    killedProcess = true;
                    process.destroy();
                }
            }
        } catch (final Exception e) {
            caught = e;
            DebugUtils.handleException("Getting the exit value of the process failed", e);
        } finally {
            cleanUp();
        }
    }

    /**
     * Ensures that the process is started or not already terminated
     * so we do not race with asynch executionor hang forever. The
     * caller of this method must be holding the lock on this
     */
    private void ensureStarted() { // definition of b
        while (!processStarted && caught == null) {
            try {
                this.wait();
            } catch (final InterruptedException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public synchronized boolean isWatching() { // called from test
        ensureStarted();                       // call to b
        return watch;
    }
}

public class Watchdog implements Runnable {

    @Override
    public void run() {         // called when watchdog is started
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
            fireTimeoutOccured(); // calls a
        }
    }

    protected final void fireTimeoutOccured() { // called from above
        final Enumeration<TimeoutObserver> e = observers.elements();
        while (e.hasMoreElements()) {
            e.nextElement().timeoutOccured(this); // call to a
        }
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
            return exitValue;
        } finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().remove(process);
            }
        }
    }
}

public class Exec34Test {
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
        assertFalse("Watchdog is no longer watching the process", watchdog.isWatching()); // calls b
    }
}
