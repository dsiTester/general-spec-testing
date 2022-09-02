public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setWatchdog(org.apache.commons.exec.ExecuteWatchdog)
     */
    @Override
    public void setWatchdog(final ExecuteWatchdog watchDog) { // definition of a
        this.watchdog = watchDog;
    }

    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // definition of b
        this.streamHandler = streamHandler;
    }
}

public class Exec41Test {
    /**
     *
     * When a process runs longer than allowed by a configured watchdog's
     * timeout, the watchdog tries to destroy it and then DefaultExecutor
     * tries to clean up by joining with all installed pump stream threads.
     * Problem is, that sometimes the native process doesn't die and thus
     * streams aren't closed and the stream threads do not complete.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExec41WithStreams() throws Exception {

        CommandLine cmdLine;
        ...

        final DefaultExecutor executor = new DefaultExecutor();
        final ExecuteWatchdog watchdog = new ExecuteWatchdog(2 * 1000); // allow process no more than 2 seconds
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(System.out, System.err);
        // this method was part of the patch I reverted
        // pumpStreamHandler.setAlwaysWaitForStreamThreads(false);

        executor.setWatchdog(watchdog); // call to a
        executor.setStreamHandler(pumpStreamHandler); // call to b

        final long startTime = System.currentTimeMillis();

        try {
            executor.execute(cmdLine);
        } catch (final ExecuteException e) {
            // nothing to do
        }

        final long duration = System.currentTimeMillis() - startTime;

        System.out.println("Process completed in " + duration + " millis; below is its output");

        if (watchdog.killedProcess()) {
            System.out.println("Process timed out and was killed by watchdog.");
        }

        assertTrue("The process was killed by the watchdog", watchdog.killedProcess());
        assertTrue("Skipping the Thread.join() did not work", duration < 9000);
    }

}
