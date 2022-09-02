public class DefaultExecuteResultHandler implements ExecuteResultHandler {
    /**
     * Causes the current thread to wait, if necessary, until the
     * process has terminated. This method returns immediately if
     * the process has already terminated. If the process has
     * not yet terminated, the calling thread will be blocked until the
     * process exits.
     *
     * @param timeoutMillis the maximum time to wait in milliseconds
     * @throws  InterruptedException if the current thread is
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    public void waitFor(final long timeoutMillis) throws InterruptedException { // definition of a

        final long untilMillis = System.currentTimeMillis() + timeoutMillis;

        while (!hasResult() && System.currentTimeMillis() < untilMillis) {
            Thread.sleep(SLEEP_TIME_MS);
        }
    }

    /**
     * Get the {@code exitValue} of the process.
     *
     * @return Returns the exitValue.
     * @throws IllegalStateException if the process has not exited yet
     */
    public int getExitValue() { // definition of b

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exitValue;
    }


}

public class DefaultExecutorTest {
    @Test
    public void testExecuteAsyncWithError() throws Exception {
        final CommandLine cl = new CommandLine(errorTestScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler);
        resultHandler.waitFor(2000); // call to a
        assertTrue(resultHandler.hasResult());                    // assertion fails here
        assertTrue(exec.isFailure(resultHandler.getExitValue())); // call to b
        assertNotNull(resultHandler.getException());
        assertEquals("FOO..", baos.toString().trim());
    }
}
