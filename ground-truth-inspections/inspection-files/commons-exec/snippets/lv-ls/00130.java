public class DefaultExecutor implements Executor {

    /** @see org.apache.commons.exec.Executor#setExitValues(int[]) */
    @Override
    public void setExitValues(final int[] values) { // definition of a
        this.exitValues = values == null ? null : (int[]) values.clone();
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
    @Test
    public void testExecuteWithCustomExitValue1() throws Exception { // validated test
        exec.setExitValue(ERROR_STATUS); // calls a
        final CommandLine cl = new CommandLine(errorTestScript);
        exec.execute(cl);       // calls b
    }

    /**
     * Invoke the error script but define that SUCCESS_STATUS is a bad
     * exit value and therefore an exception should be thrown.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExecuteWithCustomExitValue2() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(errorTestScript);
        exec.setExitValue(SUCCESS_STATUS); // calls a
        try{
            exec.execute(cl);   // calls b
            fail("Must throw ExecuteException");
        } catch (final ExecuteException e) {
            assertTrue(exec.isFailure(e.getExitValue()));
        }
    }

}
