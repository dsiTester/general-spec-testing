public interface Executor {
    /**
     * Define the {@code exitValue} of the process to be considered
     * successful. If a different exit value is returned by
     * the process then {@link org.apache.commons.exec.Executor#execute(CommandLine)}
     * will throw an {@link org.apache.commons.exec.ExecuteException}
     *
     * @param value the exit code representing successful execution
     */
    void setExitValue(final int value); // a
}

public class DefaultExecutor implements Executor {
    /** @see org.apache.commons.exec.Executor#setExitValue(int) */
    @Override
    public void setExitValue(final int value) { // only implementation of a
        this.setExitValues(new int[] {value});
    }

    /**
     * Close the streams belonging to the given Process.
     *
     * @param process the <CODE>Process</CODE>.
     */
    private void closeProcessStreams(final Process process) { // definition of b

        try {
            process.getInputStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }

        try {
            process.getOutputStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }

        try {
            process.getErrorStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }
    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from DefaultExecutor.execute()

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

        ...

        streams.start();

        try {

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
            ...
            closeProcessStreams(process); // call to b

            if (getExceptionCaught() != null) {
                throw getExceptionCaught();
            }
            ...
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
    public void setExitValues(final int[] values) { // called from a
        this.exitValues = values == null ? null : (int[]) values.clone();
    }
}

public class DefaultExecutorTest {
    /**
     * Invoke the error script but define that the ERROR_STATUS is a good
     * exit value and therefore no exception should be thrown.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExecuteWithCustomExitValue1() throws Exception {
        exec.setExitValue(ERROR_STATUS); // call to a
        final CommandLine cl = new CommandLine(errorTestScript);
        exec.execute(cl);       // calls b
    }
}
