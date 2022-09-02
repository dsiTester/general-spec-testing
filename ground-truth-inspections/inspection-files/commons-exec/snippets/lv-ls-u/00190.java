public interface ExecuteStreamHandler {

    /**
     * Start handling of the streams.
     *
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void start() throws IOException; // a

    /**
     * Stop handling of the streams - will not be restarted. Will wait for pump threads to complete.
     *
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void stop() throws IOException; // b
}

public class PumpStreamHandler implements ExecuteStreamHandler {
    /**
     * Start the <CODE>Thread</CODE>s.
     */
    @Override
    public void start() {       // only implementation of a
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

    /**
     * Stop pumping the streams. When a timeout is specified it it is not guaranteed that the
     * pumper threads are cleanly terminated.
     */
    @Override
    public void stop() throws IOException { // only implementation of b

        if (inputStreamPumper != null) {
            inputStreamPumper.stopProcessing();
        }

        stopThread(outputThread, stopTimeout);
        stopThread(errorThread, stopTimeout);
        stopThread(inputThread, stopTimeout);

        if (err != null && err != out) {
            try {
                err.flush();
            } catch (final IOException e) {
                final String msg = "Got exception while flushing the error stream : " + e.getMessage();
                DebugUtils.handleException(msg, e);
            }
        }

        if (out != null) {
            try {
                out.flush();
            } catch (final IOException e) {
                final String msg = "Got exception while flushing the output stream";
                DebugUtils.handleException(msg, e);
            }
        }

        if (caught != null) {
            throw caught;
        }
    }

}

public class DefaultExecutor implements Executor {

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from DefaultExecutor.execute()

        final Process process;
        exceptionCaught = null;

        ...
        try {                   // sets up input/output/error streams
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

        streams.start();        // call to a
        try {

            try {
                streams.stop(); // call to b
            }
            catch (final IOException e) {
                setExceptionCaught(e);
            }
            ...
        }
        ...
        finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().remove(process);
            }
        }
    }
}

public class DefaultExecutorTest {

    /**
     * Call a script to dump the environment variables of the subprocess
     * after adding a custom environment variable.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testAddEnvironmentVariables() throws Exception { // validated test
        final Map<String, String> myEnvVars = new HashMap<>(EnvironmentUtils.getProcEnvironment());
        myEnvVars.put("NEW_VAR","NEW_VAL");
        exec.execute(new CommandLine(environmentSript), myEnvVars); // calls a and b
        final String environment = baos.toString().trim();
        assertTrue("Expecting NEW_VAR in "+environment,environment.indexOf("NEW_VAR") >= 0); // assertion failed
        assertTrue("Expecting NEW_VAL in "+environment,environment.indexOf("NEW_VAL") >= 0);
    }

    @Test
    public void testExecuteWithNullOutErr() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(testScript);
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(null, null);
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        final int exitValue = executor.execute(cl); // calls a and b
        assertFalse(exec.isFailure(exitValue));
    }

    /**
     * The test script reads an argument from {@code stdin} and prints
     * the result to stdout. To make things slightly more interesting
     * we are using an asynchronous process.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testStdInHandling() throws Exception { // unknown verdict test
        // newline not needed; causes problems for VMS
        final ByteArrayInputStream bais = new ByteArrayInputStream("Foo".getBytes());
        final CommandLine cl = new CommandLine(this.stdinSript);
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(this.baos, System.err, bais);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        final Executor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.execute(cl, resultHandler); // calls a and b (b doesn't get called in the DSI experiment because the process hung)

        resultHandler.waitFor(WAITFOR_TIMEOUT);
        assertTrue("ResultHandler received a result", resultHandler.hasResult());

        assertFalse(exec.isFailure(resultHandler.getExitValue()));
        final String result = baos.toString();
        assertTrue("Result '" + result + "' should contain 'Hello Foo!'", result.indexOf("Hello Foo!") >= 0);
    }

}
