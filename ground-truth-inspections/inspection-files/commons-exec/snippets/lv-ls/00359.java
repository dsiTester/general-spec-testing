public interface ExecuteStreamHandler {
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
     * Create the pump to handle process output.
     *
     * @param is the <CODE>InputStream</CODE>.
     * @param os the <CODE>OutputStream</CODE>.
     */
    protected void createProcessOutputPump(final InputStream is, final OutputStream os) { // definition of a
        outputThread = createPump(is, os);
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

    @Override
    public void setProcessOutputStream(final InputStream is) { // called from DefaultExecutor.executeInternal()
        if (out != null) {
            createProcessOutputPump(is, out); // call to a
        }
    }

    /**
     * Creates a stream pumper to copy the given input stream to the given
     * output stream. When the 'os' is an PipedOutputStream we are closing
     * 'os' afterwards to avoid an IOException ("Write end dead").
     *
     * @param is the input stream to copy from
     * @param os the output stream to copy into
     * @return the stream pumper thread
     */
    protected Thread createPump(final InputStream is, final OutputStream os) { // called from a
        final boolean closeWhenExhausted = os instanceof PipedOutputStream ? true : false;
        return createPump(is, os, closeWhenExhausted);
    }
}

public class DefaultExecutor implements Executor {
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException {

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
            streams.setProcessOutputStream(process.getInputStream()); // calls a
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        streams.start();        // call to b

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

    @Test
    public void testExecute() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a and b
        assertEquals("FOO..", baos.toString().trim()); // assertion fails here
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

    @Test
    public void testExecuteWithRedirectedStreams() throws Exception { // invalidated test
        if (OS.isFamilyUnix()) {
            final FileInputStream fis = new FileInputStream("./NOTICE.txt");
            final CommandLine cl = new CommandLine(redirectScript);
            final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(baos, baos, fis);
            final DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(new File("."));
            executor.setStreamHandler(pumpStreamHandler);
            final int exitValue = executor.execute(cl); // calls a and b
            fis.close();
            final String result = baos.toString().trim();
            assertTrue(result, result.indexOf("Finished reading from stdin") > 0);
            assertFalse("exitValue=" + exitValue, exec.isFailure(exitValue));
        } ...
    }

}
