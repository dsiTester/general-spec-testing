public interface ExecuteStreamHandler {
    /**
     * Install a handler for the error stream of the subprocess.
     *
     * @param is
     *            input stream to read from the error stream from the subprocess
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void setProcessErrorStream(InputStream is) throws IOException; // a
}

public class PumpStreamHandler implements ExecuteStreamHandler {
    /**
     * Set the <CODE>InputStream</CODE> from which to read the standard error
     * of the process.
     *
     * @param is the <CODE>InputStream</CODE>.
     */
    @Override
    public void setProcessErrorStream(final InputStream is) { // only implementation of a
        if (err != null) {
            createProcessErrorPump(is, err); // call to b
        }
    }

    /**
     * Create the pump to handle error output.
     *
     * @param is the <CODE>InputStream</CODE>.
     * @param os the <CODE>OutputStream</CODE>.
     */
    protected void createProcessErrorPump(final InputStream is, final OutputStream os) { // definition of b
        errorThread = createPump(is, os);
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
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream()); // call to a
        } catch (final IOException e) {
            process.destroy();
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        streams.start();
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
    public void testExecute() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a and b
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

}
