public interface ExecuteStreamHandler {
    /**
     * Install a handler for the output stream of the subprocess.
     *
     * @param is
     *            input stream to read from the error stream from the subprocess
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void setProcessOutputStream(InputStream is) throws IOException; // a

    /**
     * Start handling of the streams.
     *
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void start() throws IOException; // b
}

public class PumpStreamHandler implements ExecuteStreamHandler {

    /**
     * Set the <CODE>InputStream</CODE> from which to read the standard output
     * of the process.
     *
     * @param is the <CODE>InputStream</CODE>.
     */
    @Override
    public void setProcessOutputStream(final InputStream is) { // only implementation of a
        if (out != null) {
            createProcessOutputPump(is, out);
        }
    }

    /**
     * Start the <CODE>Thread</CODE>s.
     */
    @Override
    public void start() {       // only implementation of b
        if (outputThread != null) {
            outputThread.start();
        }
        if (errorThread != null) {
            errorThread.start();
        }
        if (inputThread != null) {
            inputThread.start();
        }
    }

    protected void createProcessOutputPump(final InputStream is, final OutputStream os) { // called from a
        outputThread = createPump(is, os);
    }

}

public class DefaultExecutor implements Executor {

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called indirectly from DefaultExecutor.execute(CommandLine)

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
            streams.setProcessOutputStream(process.getInputStream()); // call to a
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        streams.start();        // call to b
        ...
        try {
            ...
            try {
                streams.stop();
            }
            catch (final IOException e) {
                setExceptionCaught(e);
            }
            ...
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
    public void testExecute() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a and b
        assertEquals("FOO..", baos.toString().trim()); // assertion fails because baos didn't get anything because of the delay of a
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

    @Test
    public void testExecuteWithError() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(errorTestScript);

        try{
            exec.execute(cl);   // calls a and b
            fail("Must throw ExecuteException");
        } catch (final ExecuteException e) {
            assertTrue(exec.isFailure(e.getExitValue()));
        }
    }

}
