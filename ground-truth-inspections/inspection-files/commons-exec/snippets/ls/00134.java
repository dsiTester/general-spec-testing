public class DefaultExecutor implements Executor {
    /** @see org.apache.commons.exec.Executor#setExitValues(int[]) */
    @Override
    public void setExitValues(final int[] values) { // definition of a
        this.exitValues = values == null ? null : (int[]) values.clone();
    }

    /**
     * @see org.apache.commons.exec.Executor#setWatchdog(org.apache.commons.exec.ExecuteWatchdog)
     */
    @Override
    public void setWatchdog(final ExecuteWatchdog watchDog) { // definition of b
        this.watchdog = watchDog;
    }

    /** @see org.apache.commons.exec.Executor#setExitValue(int) */
    @Override
    public void setExitValue(final int value) { // called from test
        this.setExitValues(new int[] {value});
    }
}

public interface Executor {
    /**
     * Set the watchdog used to kill of processes running,
     * typically, too long time.
     *
     * @param watchDog the watchdog
     */
    void setWatchdog(ExecuteWatchdog watchDog); // b
}

public class TutorialTest {
    @Test
    public void testTutorialExample() throws Exception {

        final long printJobTimeout = 15000;
        final boolean printInBackground = false;
        final File pdfFile = new File("/Documents and Settings/foo.pdf");

        PrintResultHandler printResult;

        try {
            // printing takes around 10 seconds
            System.out.println("[main] Preparing print job ...");
            printResult = print(pdfFile, printJobTimeout, printInBackground); // calls a and b
            System.out.println("[main] Successfully sent the print job ...");
        }
        catch (final Exception e) {
            e.printStackTrace();
            fail("[main] Printing of the following document failed : " + pdfFile.getAbsolutePath());
            throw e;
        }

        // come back to check the print result
        System.out.println("[main] Test is exiting but waiting for the print job to finish...");
        printResult.waitFor();
        System.out.println("[main] The print job has finished ...");
    }

    public PrintResultHandler print(final File file, final long printJobTimeout, final boolean printInBackground)
            throws IOException {

        int exitValue;
        ExecuteWatchdog watchdog = null;
        PrintResultHandler resultHandler;
        ...
        // create the executor and consider the exitValue '1' as success
        final Executor executor = new DefaultExecutor();
        executor.setExitValue(1); // calls a

        // create a watchdog if requested
        if (printJobTimeout > 0) {
            watchdog = new ExecuteWatchdog(printJobTimeout);
            executor.setWatchdog(watchdog); // call to b
        }

        // pass a "ExecuteResultHandler" when doing background printing
        if (printInBackground) {
            System.out.println("[print] Executing non-blocking print job  ...");
            resultHandler = new PrintResultHandler(watchdog);
            executor.execute(commandLine, resultHandler); // throws ExecuteException if a is not called by this point
        }
        else {
            System.out.println("[print] Executing blocking print job  ...");
            exitValue = executor.execute(commandLine);
            resultHandler = new PrintResultHandler(exitValue);
        }

        return resultHandler;
    }

}
