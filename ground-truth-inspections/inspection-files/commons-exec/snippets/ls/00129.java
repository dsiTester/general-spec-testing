public class DefaultExecutor implements Executor {

    /** @see org.apache.commons.exec.Executor#setExitValues(int[]) */
    @Override
    public void setExitValues(final int[] values) { // definition of a
        this.exitValues = values == null ? null : (int[]) values.clone();
    }

    /**
     * Get the first IOException being thrown.
     *
     * @return the first IOException being caught
     */
    private IOException getExceptionCaught() { // definition of b
        return this.exceptionCaught;
    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from DefaultExecutor.execute()

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
            closeProcessStreams(process);

            if (getExceptionCaught() != null) { // call to b
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
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteWithCustomExitValue1() throws Exception {
        exec.setExitValue(ERROR_STATUS); // calls a
        final CommandLine cl = new CommandLine(errorTestScript);
        exec.execute(cl);       // calls b
    }
}
