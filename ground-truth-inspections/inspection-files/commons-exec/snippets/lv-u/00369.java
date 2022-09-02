public interface ProcessDestroyer {
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
     * Registers this {@code ProcessDestroyer} as a shutdown hook, uses reflection to ensure pre-JDK 1.3 compatibility.
     */
    private void addShutdownHook() { // definition of a
        if (!running) {
            destroyProcessThread = new ProcessDestroyerImpl();
            Runtime.getRuntime().addShutdownHook(destroyProcessThread);
            added = true;
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
                removeShutdownHook(); // removes processDestroyer from shutdown hooks
            }
            return processRemoved;
        }
    }

    @Override
    public boolean add(final Process process) { // called from DefaultExecutorTest.executeInternal()
        synchronized (processes) {
            // if this list is empty, register the shutdown hook
            if (processes.isEmpty()) {
                addShutdownHook(); // call to a
            }
            processes.addElement(process);
            return processes.contains(process);
        }
    }

    /**
     * Removes this {@code ProcessDestroyer} as a shutdown hook, uses reflection to ensure pre-JDK 1.3 compatibility
     */
    private void removeShutdownHook() { // called from b
        if (added && !running) {
            final boolean removed = Runtime.getRuntime().removeShutdownHook(destroyProcessThread);
            if (!removed) {
                System.err.println("Could not remove shutdown hook");
            }
            /*
             * start the hook thread, a unstarted thread may not be eligible for garbage collection Cf.:
             * http://developer.java.sun.com/developer/ bugParade/bugs/4533087.html
             */

            destroyProcessThread.setShouldDestroy(false);
            destroyProcessThread.start();
            // this should return quickly, since it basically is a NO-OP.
            try {
                destroyProcessThread.join(20000);
            } catch (final InterruptedException ie) {
                // the thread didn't die in time
                // it should not kill any processes unexpectedly
            }
            destroyProcessThread = null;
            added = false;
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
              this.getProcessDestroyer().add(process); // calls a
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
      assertTrue(processDestroyer.isEmpty());
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false); // assertion fails here
    }

    @Test
    public void testExecuteAsyncWithProcessDestroyer() throws Exception { // unknown test

      final CommandLine cl = new CommandLine(foreverTestScript);
      final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
      final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
      final ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);

      assertTrue(exec.getProcessDestroyer() == null);
      assertTrue(processDestroyer.isEmpty());
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false);

      exec.setWatchdog(watchdog);
      exec.setProcessDestroyer(processDestroyer);
      exec.execute(cl, handler); // calls a and b

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
