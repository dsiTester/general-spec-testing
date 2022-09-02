public class DefaultExecuteResultHandler implements ExecuteResultHandler {
    /**
     * Get the {@code exitValue} of the process.
     *
     * @return Returns the exitValue.
     * @throws IllegalStateException if the process has not exited yet
     */
    public int getExitValue() { // definition of a

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exitValue;
    }

    /**
     * Get the {@code exception} causing the process execution to fail.
     *
     * @return Returns the exception.
     * @throws IllegalStateException if the process has not exited yet
     */
    public ExecuteException getException() { // definition of b

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exception;
    }

}

public class DefaultExecutorTest {
    @Test
    public void testExecuteAsyncWithError() throws Exception {
        final CommandLine cl = new CommandLine(errorTestScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler);
        resultHandler.waitFor(2000);
        assertTrue(resultHandler.hasResult());
        assertTrue(exec.isFailure(resultHandler.getExitValue())); // call to a
        assertNotNull(resultHandler.getException());              // call to b
        assertEquals("FOO..", baos.toString().trim());
    }
}
