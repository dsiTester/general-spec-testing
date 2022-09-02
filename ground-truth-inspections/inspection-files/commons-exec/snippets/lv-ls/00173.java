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
     * Set the <CODE>InputStream</CODE> from which to read the standard error
     * of the process.
     *
     * @param is the <CODE>InputStream</CODE>.
     */
    @Override
    public void setProcessErrorStream(final InputStream is) { // only implementation of a
        if (err != null) {
            createProcessErrorPump(is, err);
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

    protected void createProcessErrorPump(final InputStream is, final OutputStream os) { // called from a
        errorThread = createPump(is, os);
    }

    protected void stopThread(final Thread thread, final long timeoutMillis) { // called from b

        if (thread != null) {
            try {
                if (timeoutMillis == 0) {
                    thread.join();
                } else {
                    final long timeToWaitMillis = timeoutMillis + STOP_TIMEOUT_ADDITION_MILLIS;
                    final long startTimeMillis = System.currentTimeMillis();
                    thread.join(timeToWaitMillis);
                    if (System.currentTimeMillis() > startTimeMillis + timeToWaitMillis) {
                        final String msg = "The stop timeout of " + timeoutMillis + " ms was exceeded";
                        caught = new ExecuteException(msg, Executor.INVALID_EXITVALUE);
                    }
                }
            } catch (final InterruptedException e) {
                thread.interrupt();
            }
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
                streams.stop(); // call to b
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
    /**
     * Start a process with redirected streams - stdin of the newly
     * created process is connected to a FileInputStream whereas
     * the "redirect" script reads all lines from stdin and prints
     * them on stdout. Furthermore the script prints a status
     * message on stderr.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExecuteWithRedirectedStreams() throws Exception { // validated test
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
            assertTrue(result, result.indexOf("Finished reading from stdin") > 0); // assertion fails here
            assertFalse("exitValue=" + exitValue, exec.isFailure(exitValue));
        } else if (OS.isFamilyWindows()) {
            System.err
                    .println("The code samples to do that in windows look like a joke ... :-( .., no way I'm doing that");
            System.err.println("The test 'testExecuteWithRedirectedStreams' does not support the following OS : "
                    + System.getProperty("os.name"));
        } else {
            System.err.println("The test 'testExecuteWithRedirectedStreams' does not support the following OS : "
                    + System.getProperty("os.name"));
        }
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
