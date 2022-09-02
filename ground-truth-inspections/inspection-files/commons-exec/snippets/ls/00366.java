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
     * Creates a stream pumper to copy the given input stream to the given
     * output stream.
     *
     * @param is the System.in input stream to copy from
     * @param os the output stream to copy into
     * @return the stream pumper thread
     */
    private Thread createSystemInPump(final InputStream is, final OutputStream os) { // definition of a
        inputStreamPumper = new InputStreamPumper(is, os);
        final Thread result = new Thread(inputStreamPumper, "Exec Input Stream Pumper");
        result.setDaemon(true);
        return result;
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
    public void setProcessInputStream(final OutputStream os) { // called from DefaultExecutor.executeInternal()
        if (input != null) {
            if (input == System.in) {
                inputThread = createSystemInPump(input, os); // call to a
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
            streams.setProcessInputStream(process.getOutputStream()); // calls a
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
                streams.stop(); // call to b
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

public class Exec33Test {

    private final Executor exec = new DefaultExecutor();
    private final File testDir = new File("src/test/scripts");
    private final File testScript = TestUtil.resolveScriptForOS(testDir + "/test");

    @Test
    public void testExec33() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(System.out, System.err, System.in);
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        final int exitValue = executor.execute(cl); // calls a and b
        assertFalse(exec.isFailure(exitValue));
    }
}

/*
  Script run by the test:

  echo FOO.$TEST_ENV_VAR.$1
*/
