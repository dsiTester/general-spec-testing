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
     * Start handling of the streams.
     *
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void start() throws IOException; // b
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
            streams.setProcessOutputStream(process.getInputStream());
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
    }

}

public class DefaultExecutorTest {
    @Test
    public void testStdInHandling() throws Exception {
        // newline not needed; causes problems for VMS
        final ByteArrayInputStream bais = new ByteArrayInputStream("Foo".getBytes());
        final CommandLine cl = new CommandLine(this.stdinSript);
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(this.baos, System.err, bais);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        final Executor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.execute(cl, resultHandler); // calls a and b

        resultHandler.waitFor(WAITFOR_TIMEOUT);
        assertTrue("ResultHandler received a result", resultHandler.hasResult()); // assertion fails here

        assertFalse(exec.isFailure(resultHandler.getExitValue()));
        final String result = baos.toString();
        assertTrue("Result '" + result + "' should contain 'Hello Foo!'", result.indexOf("Hello Foo!") >= 0);
    }

    @Test
    public void testExecute() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a and b
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

}

/*
  Below is the script run by the validating test:

echo "What's your name? : "
read answer
echo "Hello $answer!"

*/
