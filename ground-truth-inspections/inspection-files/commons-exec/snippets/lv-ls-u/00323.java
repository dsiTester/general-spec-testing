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

    /**
     * Checks whether {@code exitValue} signals a failure. If no
     * exit values are set than the default conventions of the OS is
     * used. e.g. most OS regard an exit code of '0' as successful
     * execution and everything else as failure.
     *
     * @param exitValue the exit value (return code) to be checked
     * @return {@code true} if {@code exitValue} signals a failure
     */
    boolean isFailure(final int exitValue); // b
}

public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // only implementation of a
        this.streamHandler = streamHandler;
    }

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // only implementation of b

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
    public void testExecuteAsync() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler); // uses DefaultExecutor.streamHandler
        resultHandler.waitFor(2000);
        assertTrue(resultHandler.hasResult());
        assertNull(resultHandler.getException());
        assertFalse(exec.isFailure(resultHandler.getExitValue())); // call to b
        assertEquals("FOO..", baos.toString().trim());             // assertion fails here
    }

    @Test
    public void testExecuteWithError() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(errorTestScript);

        try{
            exec.execute(cl);
            fail("Must throw ExecuteException");
        } catch (final ExecuteException e) {
            assertTrue(exec.isFailure(e.getExitValue()));   // call to b
        }
    }

    @Test
    public void testExecute() throws Exception { // unknown test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl);
        assertEquals("FOO..", baos.toString().trim()); // assertion fails here
        assertFalse(exec.isFailure(exitValue)); // call to b
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

}
