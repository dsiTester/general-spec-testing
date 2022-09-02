public interface Executor {
    /**
     * Set a custom the StreamHandler used for providing
     * input and retrieving the output. If you don't provide
     * a proper stream handler the executed process might block
     * when writing to stdout and/or stderr (see
     * {@link java.lang.Process Process}).
     *
     * @param streamHandler the stream handler
     */
    void setStreamHandler(ExecuteStreamHandler streamHandler); // a

    /**
     * Methods for starting asynchronous execution. The child process inherits
     * all environment variables of the parent process. Result provided to
     * callback handler.
     *
     * @param command the command to execute
     * @param handler capture process termination and exit code
     * @throws ExecuteException execution of subprocess failed
     */
    void execute(CommandLine command, ExecuteResultHandler handler)
        throws ExecuteException, IOException; // b
}

public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // only implementation of a
        this.streamHandler = streamHandler;
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine,
     *      org.apache.commons.exec.ExecuteResultHandler)
     */
    @Override
    public void execute(final CommandLine command, final ExecuteResultHandler handler)
            throws ExecuteException, IOException { // only implementation of b
        execute(command, null, handler);           // uses DefaultExecutor.streamHandler
    }

    @Override
    public void execute(final CommandLine command, final Map<String, String> environment,
            final ExecuteResultHandler handler) throws ExecuteException, IOException { // called from above

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        if (watchdog != null) {
            watchdog.setProcessNotStarted();
        }

        final Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                int exitValue = Executor.INVALID_EXITVALUE;
                try {
                    exitValue = executeInternal(command, environment, workingDirectory, streamHandler); // passing in DefaultExecutor.streamHandler, where it will be used
                    handler.onProcessComplete(exitValue);
                } catch (final ExecuteException e) {
                    handler.onProcessFailed(e);
                } catch (final Exception e) {
                    handler.onProcessFailed(new ExecuteException("Execution failed", exitValue, e));
                }
            }
        };

        this.executorThread = createThread(runnable, "Exec Default Executor");
        getExecutorThread().start(); // starting above thread
    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from the thread set up and started by above. Note that whatever value of DefaultExecutor.streamHandler by this point is set as the final argument streams.

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
                streams.stop();
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

    @Before
    public void setUp() throws Exception {

        // delete the marker file
        this.foreverOutputFile.getParentFile().mkdirs();
        if (this.foreverOutputFile.exists()) {
            this.foreverOutputFile.delete();
        }

        // prepare a ready to Executor
        this.baos = new ByteArrayOutputStream();
        this.exec.setStreamHandler(new PumpStreamHandler(baos, baos)); // call to a
    }

    @Test
    public void testExecuteAsyncWithProcessDestroyer() throws Exception {

      final CommandLine cl = new CommandLine(foreverTestScript);
      final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
      final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
      final ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);

      assertTrue(exec.getProcessDestroyer() == null);
      assertTrue(processDestroyer.isEmpty());
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false);

      exec.setWatchdog(watchdog);
      exec.setProcessDestroyer(processDestroyer);
      exec.execute(cl, handler); // call to b

      // wait for script to start
      Thread.sleep(2000);

      // our process destroyer should be initialized now
      assertNotNull("Process destroyer should exist", exec.getProcessDestroyer());
      assertEquals("Process destroyer size should be 1", 1, processDestroyer.size());
      assertTrue("Process destroyer should exist as shutdown hook", processDestroyer.isAddedAsShutdownHook());

      // terminate it and the process destroyer is detached
      watchdog.destroyProcess();
      assertTrue(watchdog.killedProcess());
      handler.waitFor(WAITFOR_TIMEOUT);
      assertTrue("ResultHandler received a result", handler.hasResult());
      assertNotNull(handler.getException());
      assertEquals("Processor Destroyer size should be 0", 0, processDestroyer.size());
      assertFalse("Process destroyer should not exist as shutdown hook", processDestroyer.isAddedAsShutdownHook());
    }

}
