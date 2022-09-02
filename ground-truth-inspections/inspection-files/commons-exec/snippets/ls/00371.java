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
    boolean add(Process process); // b

}

public class ShutdownHookProcessDestroyer implements ProcessDestroyer, Runnable {

    private final Vector<Process> processes = new Vector<>();

    /**
     * Tests emptiness (size == 0).
     *
     * @return Whether or not this is empty.
     * @since 1.4.0
     */
    public boolean isEmpty() {  // definition of a
        return size() == 0;
    }

    /**
     * Returns {@code true} if the specified {@code Process} was successfully added to the list of processes to destroy
     * upon VM exit.
     *
     * @param process the process to add
     * @return {@code true} if the specified {@code Process} was successfully added
     */
    @Override
    public boolean add(final Process process) { // definition of b
        synchronized (processes) {
            // if this list is empty, register the shutdown hook
            if (processes.isEmpty()) {
                addShutdownHook();
            }
            processes.addElement(process); // increases the size of ShutdownHookProcessDestroyer.processes
            return processes.contains(process);
        }
    }

    @Override
    public int size() {         // called from a
        return processes.size();
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
              this.getProcessDestroyer().add(process); // call to b
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
    public void testExecuteAsyncWithProcessDestroyer() throws Exception {

      final CommandLine cl = new CommandLine(foreverTestScript);
      final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
      final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
      final ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);

      assertTrue(exec.getProcessDestroyer() == null);
      assertTrue(processDestroyer.isEmpty()); // call to a
      asserttrue(processDestroyer.isAddedAsShutdownHook() == false);

      exec.setWatchdog(watchdog);
      exec.setProcessDestroyer(processDestroyer);
      exec.execute(cl, handler); // calls b

      // wait for script to start
      Thread.sleep(2000);

      // our process destroyer should be initialized now
      assertNotNull("Process destroyer should exist", exec.getProcessDestroyer());
      assertEquals("Process destroyer size should be 1", 1, processDestroyer.size());
      assertTrue("Process destroyer should exist as shutdown hook", processDestroyer.isAddedAsShutdownHook()); // assertion fails here

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
