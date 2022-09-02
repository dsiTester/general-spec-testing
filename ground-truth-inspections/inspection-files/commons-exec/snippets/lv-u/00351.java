public interface ProcessDestroyer {
    /**
     * Returns {@code true} if the specified
     * {@link java.lang.Process} was
     * successfully added to the list of processes to be destroy.
     *
     * @param process
     *      the process to add
     * @return {@code true} if the specified
     *      {@link java.lang.Process} was
     *      successfully added
     */
    boolean add(Process process); // a

    /**
     * Returns {@code true} if the specified
     * {@link java.lang.Process} was
     * successfully removed from the list of processes to be destroy.
     *
     * @param process
     *            the process to remove
     * @return {@code true} if the specified
     *      {@link java.lang.Process} was
     *      successfully removed
     */
    boolean remove(Process process); // b
}

public class ShutdownHookProcessDestroyer implements ProcessDestroyer, Runnable {
    /**
     * Returns {@code true} if the specified {@code Process} was successfully added to the list of processes to destroy
     * upon VM exit.
     *
     * @param process the process to add
     * @return {@code true} if the specified {@code Process} was successfully added
     */
    @Override
    public boolean add(final Process process) { // only implementation of a
        synchronized (processes) {
            // if this list is empty, register the shutdown hook
            if (processes.isEmpty()) {
                addShutdownHook();
            }
            processes.addElement(process);
            return processes.contains(process);
        }
    }

    /**
     * Returns {@code true} if the specified {@code Process} was successfully removed from the list of processes to
     * destroy upon VM exit.
     *
     * @param process the process to remove
     * @return {@code true} if the specified {@code Process} was successfully removed
     */
    @Override
    public boolean remove(final Process process) { // only implementation of b
        synchronized (processes) {
            final boolean processRemoved = processes.removeElement(process);
            if (processRemoved && processes.isEmpty()) {
                removeShutdownHook();
            }
            return processRemoved;
        }
    }
}

public class DefaultExecutor implements Executor {
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from DefaultExecutor.execute(CommandLine, Map) for validated test, the thread created and ran by DefaultExecutor.execute(CommandLine, Map, ExecuteStreamHandler) for invalidated test

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

        ...
        try {

            // add the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().add(process); // call to a
            }
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
              this.getProcessDestroyer().remove(process); // call to b
            }
        }
    }
}
public class DefaultExecutorTest {
    @Test
    public void testExecuteWithProcessDestroyer() throws Exception { // validated test

      final CommandLine cl = new CommandLine(testScript);
      final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
      exec.setProcessDestroyer(processDestroyer);

      assertTrue(processDestroyer.isEmpty());
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false);

      final int exitValue = exec.execute(cl); // calls a and b

      assertEquals("FOO..", baos.toString().trim());
      assertFalse(exec.isFailure(exitValue));
      asserttrue(processDestroyer.isEmpty()); // assertion fails here
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false);
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
      exec.execute(cl, handler); // creates and starts thread that calls a and b

      // wait for script to start
      Thread.sleep(2000);

      // our process destroyer should be initialized now
      assertNotNull("Process destroyer should exist", exec.getProcessDestroyer());
      assertEquals("Process destroyer size should be 1", 1, processDestroyer.size()); // assertion fails here
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
