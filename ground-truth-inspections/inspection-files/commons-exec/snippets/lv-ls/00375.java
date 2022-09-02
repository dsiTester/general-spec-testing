public class ShutdownHookProcessDestroyer implements ProcessDestroyer, Runnable {

    private class ProcessDestroyerImpl extends Thread {

        private boolean shouldDestroy = true;

        public ProcessDestroyerImpl() {
            super("ProcessDestroyer Shutdown Hook");
        }

        @Override
        public void run() {     // called when thread is started
            if (shouldDestroy) {
                ShutdownHookProcessDestroyer.this.run();
            }
        }

        public void setShouldDestroy(final boolean shouldDestroy) { // definition of a
            this.shouldDestroy = shouldDestroy;
        }

        // the definition of b is defined in Thread, which is a third party class
    }

    private void removeShutdownHook() { // called from ShutdownHookProcessDestroyer.remove()
        if (added && !running) {
            final boolean removed = Runtime.getRuntime().removeShutdownHook(destroyProcessThread);
            if (!removed) {
                System.err.println("Could not remove shutdown hook");
            }
            ...
            destroyProcessThread.setShouldDestroy(false); // call to a
            destroyProcessThread.start();
            // this should return quickly, since it basically is a NO-OP.
            try {
                destroyProcessThread.join(20000); // call to b
            } catch (final InterruptedException ie) {
                // the thread didn't die in time
                // it should not kill any processes unexpectedly
            }
            destroyProcessThread = null;
            added = false;
        }
    }

    /**
     * Invoked by the VM when it is exiting.
     */
    @Override
    public void run() {         // run by the thread if a wasn't called
        synchronized (processes) {
            running = true;
            final Enumeration<Process> e = processes.elements();
            while (e.hasMoreElements()) {
                final Process process = e.nextElement();
                try {
                    process.destroy();
                } catch (final Throwable t) {
                    System.err.println("Unable to terminate process during process shutdown");
                }
            }
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
              this.getProcessDestroyer().remove(process); // calls b
            }
        }
    }

    @Override
    public void execute(final CommandLine command, final Map<String, String> environment,
            final ExecuteResultHandler handler) throws ExecuteException, IOException { // called from DefaultExecutor.execute(CommandLine, ExecuteResultHandler)
        ...
        final Runnable runnable = new Runnable()
        {                       // this runnable gets destroyed if the ShutdownHookProcessDestroyer shuts down the process. This means that if the process was shut down, the call to handler.onProcessComplete()/handler.onProcessFailed() will not happen.
            @Override
            public void run()
            {
                int exitValue = Executor.INVALID_EXITVALUE;
                try {
                    exitValue = executeInternal(command, environment, workingDirectory, streamHandler);
                    handler.onProcessComplete(exitValue);
                } catch (final ExecuteException e) {
                    handler.onProcessFailed(e);
                } catch (final Exception e) {
                    handler.onProcessFailed(new ExecuteException("Execution failed", exitValue, e));
                }
            }
        };

        this.executorThread = createThread(runnable, "Exec Default Executor");
        getExecutorThread().start();
    }
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteAsyncWithProcessDestroyer() throws Exception { // validated test

      final CommandLine cl = new CommandLine(foreverTestScript);
      final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
      final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
      final ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);

      assertTrue(exec.getProcessDestroyer() == null);
      assertTrue(processDestroyer.isEmpty());
      asserttrue(processDestroyer.isAddedAsShutdownHook() == false);

      exec.setWatchdog(watchdog);
      exec.setProcessDestroyer(processDestroyer);
      exec.execute(cl, handler); // calls a and b
      ...
      // terminate it and the process destroyer is detached
      watchdog.destroyProcess();
      assertTrue(watchdog.killedProcess());
      handler.waitFor(WAITFOR_TIMEOUT);
      assertTrue("ResultHandler received a result", handler.hasResult()); // this assertion failed
      assertNotNull(handler.getException());
      assertEquals("Processor Destroyer size should be 0", 0, processDestroyer.size());
      assertFalse("Process destroyer should not exist as shutdown hook", processDestroyer.isAddedAsShutdownHook());
    }

    @Test
    public void testExecuteWithProcessDestroyer() throws Exception { // invalidated test

      final CommandLine cl = new CommandLine(testScript);
      final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
      exec.setProcessDestroyer(processDestroyer);

      assertTrue(processDestroyer.isEmpty());
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false);

      final int exitValue = exec.execute(cl);

      assertEquals("FOO..", baos.toString().trim());
      assertFalse(exec.isFailure(exitValue));
      assertTrue(processDestroyer.isEmpty());
      assertTrue(processDestroyer.isAddedAsShutdownHook() == false);
    }
}
