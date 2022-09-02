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
     * Create the pump to handle process output.
     *
     * @param is the <CODE>InputStream</CODE>.
     * @param os the <CODE>OutputStream</CODE>.
     */
    protected void createProcessOutputPump(final InputStream is, final OutputStream os) { // definition of b
        outputThread = createPump(is, os);
    }

    @Override
    public void setProcessOutputStream(final InputStream is) { // called from DefaultExecutor.executeInternal()
        if (out != null) {
            createProcessOutputPump(is, out); // call to b
        }
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
