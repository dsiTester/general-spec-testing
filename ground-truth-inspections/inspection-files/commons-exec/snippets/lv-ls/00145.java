public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setWatchdog(org.apache.commons.exec.ExecuteWatchdog)
     */
    @Override
    public void setWatchdog(final ExecuteWatchdog watchDog) { // definition of a
        this.watchdog = watchDog;
    }

    /**
     * Execute an internal process. If the executing thread is interrupted while waiting for the
     * child process to return the child process will be killed.
     *
     * @param command the command to execute
     * @param environment the execution environment
     * @param dir the working directory
     * @param streams process the streams (in, out, err) of the process
     * @return the exit code of the process
     * @throws IOException executing the process failed
     */
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // definition of b, shortened
        ...
        try {
            streams.setProcessInputStream(process.getOutputStream()); // streams is DefaultExecutor.streamHandler
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        }
        catch(final IOException e) {
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        ...
        streams.start();

        try {
            ...
            // associate the watchdog with the newly created process
            if (watchdog != null) {
                watchdog.start(process);
            }

            ...
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
            closeProcessStreams(process);

            if (getExceptionCaught() != null) {
                throw getExceptionCaught();
            }

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

            if (this.isFailure(exitValue)) {
                throw new ExecuteException("Process exited with an error: " + exitValue, exitValue);
            }

            return exitValue;
        } finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().remove(process);
            }
        }
    }

    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {       // called from test
        return execute(command, (Map<String, String>) null); // calls b
    }

    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // called from above

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // call to b

    }


}

public class Exec65Test extends AbstractExecTest {

    @Test(expected = ExecuteException.class, timeout = TEST_TIMEOUT) // failed because reached timeout
    public void testExec65WitSleepUsingSleepCommandDirectly() throws Exception {

        if (!OS.isFamilyUnix()) {
            throw new ExecuteException(testNotSupportedForCurrentOperatingSystem(), 0);
        }
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(WATCHDOG_TIMEOUT);
        final DefaultExecutor executor = new DefaultExecutor();
        final CommandLine command = new CommandLine("sleep");
        command.addArgument("60");
        executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
        executor.setWatchdog(watchdog); // call to a

        executor.execute(command); // calls b
    }
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
        executor.setWatchdog(watchdog); // call to a

        try {
            executor.execute(cl); // calls b
        } catch (final ExecuteException e) {
            assertFalse("Process should exit normally, not be killed by watchdog", watchdog.killedProcess());
            // If the Watchdog did not kill it, something else went wrong.
            throw e;
        }
    }
}
