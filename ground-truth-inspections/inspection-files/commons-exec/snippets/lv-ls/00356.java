public interface ExecuteStreamHandler {
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
     * Create the pump to handle error output.
     *
     * @param is the <CODE>InputStream</CODE>.
     * @param os the <CODE>OutputStream</CODE>.
     */
    protected void createProcessErrorPump(final InputStream is, final OutputStream os) { // definition of a
        errorThread = createPump(is, os);
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

    @Override
    public void setProcessErrorStream(final InputStream is) { // called by DefaultExecutor.executeInternal()
        if (err != null) {
            createProcessErrorPump(is, err); // call to a
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

    /**
     * Stopping a pumper thread. The implementation actually waits
     * longer than specified in 'timeout' to detect if the timeout
     * was indeed exceeded. If the timeout was exceeded an IOException
     * is created to be thrown to the caller.
     *
     * @param thread  the thread to be stopped
     * @param timeoutMillis the time in ms to wait to join
     */
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
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream()); // calls a
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
                streams.stop();        // call to b
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
        } ...
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
