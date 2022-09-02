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
     * Create the pump to handle error output.
     *
     * @param is the <CODE>InputStream</CODE>.
     * @param os the <CODE>OutputStream</CODE>.
     */
    protected void createProcessErrorPump(final InputStream is, final OutputStream os) { // called from b
        errorThread = createPump(is, os);
    }

    @Override
    public void setProcessErrorStream(final InputStream is) { // called from DefaultExecutor.executeInternal()
        if (err != null) {
            createProcessErrorPump(is, err); // call to b
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
            streams.setProcessInputStream(process.getOutputStream());
            streams.setProcessOutputStream(process.getInputStream()); // call to a
            streams.setProcessErrorStream(process.getErrorStream()); // calls b
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
