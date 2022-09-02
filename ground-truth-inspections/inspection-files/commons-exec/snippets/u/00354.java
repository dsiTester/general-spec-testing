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
    boolean remove(Process process); // a
}

public class ShutdownHookProcessDestroyer implements ProcessDestroyer, Runnable {
    /**
     * Returns {@code true} if the specified {@code Process} was successfully removed from the list of processes to
     * destroy upon VM exit.
     *
     * @param process the process to remove
     * @return {@code true} if the specified {@code Process} was successfully removed
     */
    @Override
    public boolean remove(final Process process) { // only implementation of a
        synchronized (processes) {
            final boolean processRemoved = processes.removeElement(process);
            if (processRemoved && processes.isEmpty()) {
                removeShutdownHook(); // call to b
            }
            return processRemoved;
        }
    }

    /**
     * Removes this {@code ProcessDestroyer} as a shutdown hook, uses reflection to ensure pre-JDK 1.3 compatibility
     */
    private void removeShutdownHook() { // definition of b
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
              this.getProcessDestroyer().add(process);
            }
            ...

            return exitValue;
        } finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().remove(process); // call to a
            }
        }
    }
}
public class DefaultExecutorTest {
    @Test
    public void testExecuteWithProcessDestroyer() throws Exception {

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
}
