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
     * Stop handling of the streams - will not be restarted. Will wait for pump threads to complete.
     *
     * @throws IOException
     *             thrown when an I/O exception occurs.
     */
    void stop() throws IOException; // b

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

    @Test
    public void testExecute() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a and b
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

    @Test
    public void testStdInHandling() throws Exception { // unknown test
        // newline not needed; causes problems for VMS
        final ByteArrayInputStream bais = new ByteArrayInputStream("Foo".getBytes());
        final CommandLine cl = new CommandLine(this.stdinSript);
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(this.baos, System.err, bais);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        final Executor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.execute(cl, resultHandler); // calls a and b asynchronously

        resultHandler.waitFor(WAITFOR_TIMEOUT);
        assertTrue("ResultHandler received a result", resultHandler.hasResult()); // assertion fails here

        assertFalse(exec.isFailure(resultHandler.getExitValue()));
        final String result = baos.toString();
        assertTrue("Result '" + result + "' should contain 'Hello Foo!'", result.indexOf("Hello Foo!") >= 0);
    }

}

/*Script called by invalidated test

echo FOO.$TEST_ENV_VAR.$1

*/

/*
  Below is the script run by the unknown test:

echo "What's your name? : "
read answer
echo "Hello $answer!"

*/
