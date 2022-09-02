public interface Executor {
    /**
     * Set a custom the StreamHandler used for providing
     * input and retrieving the output. If you don't provide
     * a proper stream handler the executed process might block
     * when writing to stdout and/or stderr (see
     * {@link java.lang.Process Process}).
     *
     * @param streamHandler the stream handler
     */
    void setStreamHandler(ExecuteStreamHandler streamHandler); // a
}

public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // only implementation of a
        this.streamHandler = streamHandler;
    }

    /** @see org.apache.commons.exec.Executor#setExitValues(int[]) */
    @Override
    public void setExitValues(final int[] values) { // definition of b
        this.exitValues = values == null ? null : (int[]) values.clone();
    }

    /** @see org.apache.commons.exec.Executor#setExitValue(int) */
    @Override
    public void setExitValue(final int value) { // called from test
        this.setExitValues(new int[] {value});  // call to b
    }
}

public class DefaultExecutorTest {

    @Before
    public void setUp() throws Exception {

        // delete the marker file
        this.foreverOutputFile.getParentFile().mkdirs();
        if (this.foreverOutputFile.exists()) {
            this.foreverOutputFile.delete();
        }

        // prepare a ready to Executor
        this.baos = new ByteArrayOutputStream();
        this.exec.setStreamHandler(new PumpStreamHandler(baos, baos)); // call to a
    }

    @Test
    public void testExecuteWithCustomExitValue1() throws Exception {
        exec.setExitValue(ERROR_STATUS); // calls b
        final CommandLine cl = new CommandLine(errorTestScript);
        exec.execute(cl);
    }
}
