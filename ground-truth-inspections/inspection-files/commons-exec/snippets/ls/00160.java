public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setWorkingDirectory(java.io.File)
     */
    @Override
    public void setWorkingDirectory(final File dir) { // definition of a
        this.workingDirectory = dir;
    }

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // definition of b

        if (this.exitValues == null) {
            return false;
        }
        if (this.exitValues.length == 0) {
            return this.launcher.isFailure(exitValue);
        }
        for (final int exitValue2 : this.exitValues) {
            if (exitValue2 == exitValue) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {                                    // called from test
        return execute(command, (Map<String, String>) null); // calls b
    }

    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // called from above

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // calls b

    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from above
        ...
        try {
            process = this.launch(command, environment, dir);
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

            ...

            try {
                streams.stop();
            }
            catch (final IOException e) {
                setExceptionCaught(e);
            }

            closeProcessStreams(process);

            if (getExceptionCaught() != null) {
                throw getExceptionCaught();
            }

            ...
            if (this.isFailure(exitValue)) { // call to b
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
    public void testExecuteWatchdogVeryLongTimeout() throws Exception {
        final long timeout = Long.MAX_VALUE;

        final CommandLine cl = new CommandLine(testScript);
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(".")); // call to a
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
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
