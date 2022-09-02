public interface ExecuteStreamHandler {
    /**
     * Install a handler for the input stream of the subprocess.
     *
     * @param os
     *            output stream to write to the standard input stream of the subprocess
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void setProcessInputStream(OutputStream os) throws IOException; // a

    /**
     * Install a handler for the output stream of the subprocess.
     *
     * @param is
     *            input stream to read from the error stream from the subprocess
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void setProcessOutputStream(InputStream is) throws IOException; // b
}

public class PumpStreamHandler implements ExecuteStreamHandler {
    /**
     * Set the <CODE>OutputStream</CODE> by means of which input can be sent
     * to the process.
     *
     * @param os the <CODE>OutputStream</CODE>.
     */
    @Override
    public void setProcessInputStream(final OutputStream os) { // only implementation of a
        if (input != null) {
            if (input == System.in) {
                inputThread = createSystemInPump(input, os);
            } else {
                inputThread = createPump(input, os, true);
            }
        } else {
            try {
                os.close();
            } catch (final IOException e) {
                final String msg = "Got exception while closing output stream";
                DebugUtils.handleException(msg, e);
            }
        }
    }

    /**
     * Set the <CODE>InputStream</CODE> from which to read the standard output
     * of the process.
     *
     * @param is the <CODE>InputStream</CODE>.
     */
    @Override
    public void setProcessOutputStream(final InputStream is) { // definition of b
        if (out != null) {
            createProcessOutputPump(is, out);
        }
    }

    protected void createProcessOutputPump(final InputStream is, final OutputStream os) { // called from b
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
            streams.setProcessInputStream(process.getOutputStream()); // call to a
            streams.setProcessOutputStream(process.getInputStream()); // call to b
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        streams.start();
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
