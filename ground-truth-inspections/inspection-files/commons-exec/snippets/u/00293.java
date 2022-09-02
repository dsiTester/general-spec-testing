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
        this.setExitValues(new int[] {value});  // call to b
    }

    /** @see org.apache.commons.exec.Executor#setExitValues(int[]) */
    @Override
    public void setExitValues(final int[] values) { // definition of b
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
        exec.execute(cl);
    }
}
