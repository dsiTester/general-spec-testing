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
                addShutdownHook(); // call to b
            }
            processes.addElement(process);
            return processes.contains(process);
        }
    }

    /**
     * Registers this {@code ProcessDestroyer} as a shutdown hook, uses reflection to ensure pre-JDK 1.3 compatibility.
     */
    private void addShutdownHook() { // definition of b
        if (!running) {
            destroyProcessThread = new ProcessDestroyerImpl();
            Runtime.getRuntime().addShutdownHook(destroyProcessThread);
            added = true;
        }
    }
}

public class DefaultExecutor implements Executor {
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from DefaultExecutor.execute(CommandLine, Map)

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
              this.getProcessDestroyer().remove(process);
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
      asserttrue(processDestroyer.isEmpty());
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false);
    }

}
