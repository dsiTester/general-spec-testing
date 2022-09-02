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
     * Methods for starting synchronous execution. The child process inherits
     * all environment variables of the parent process.
     *
     * @param command the command to execute
     * @return process exit value
     * @throws ExecuteException execution of subprocess failed or the
     *          subprocess returned a exit value indicating a failure
     *          {@link Executor#setExitValue(int)}.
     */
    int execute(CommandLine command)
        throws ExecuteException, IOException; // b
}

public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // only implementation of a
        this.streamHandler = streamHandler;
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine)
     */
    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {                                    // only implementation of b
        return execute(command, (Map<String, String>) null); // uses DefaultExecutor.streamHandler
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine, java.util.Map)
     */
    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // called from above

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // passing in DefaultExecutor.streamHandler

    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from above. DefaultExecutor.streamHandler is passed in as the argument streams.

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

        try {
            streams.setProcessInputStream(process.getOutputStream());
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

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
    public void testExecute() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // call to b
        assertEquals("FOO..", baos.toString().trim()); // assertion fails here
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

    @Test
    public void testExecuteWithCustomExitValue1() throws Exception { // invalidated test
        exec.setExitValue(ERROR_STATUS);
        final CommandLine cl = new CommandLine(errorTestScript);
        exec.execute(cl); // call to b
    }

}
