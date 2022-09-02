public class DefaultExecuteResultHandler implements ExecuteResultHandler {

    /**
     * @see org.apache.commons.exec.ExecuteResultHandler#onProcessComplete(int)
     */
    @Override
    public void onProcessComplete(final int exitValue) { // definition of a
        this.exitValue = exitValue;
        this.exception = null;
        this.hasResult = true;
    }

    /**
     * Has the process exited and a result is available, i.e. exitCode or exception?
     *
     * @return true if a result of the execution is available
     */
    public boolean hasResult() { // definition of b
        return hasResult;
    }

    public void waitFor() throws InterruptedException { // called from test

        while (!hasResult()) {  // call to b
            Thread.sleep(SLEEP_TIME_MS);
        }
    }

}

public class TutorialTest {
    private class PrintResultHandler extends DefaultExecuteResultHandler {

        private ExecuteWatchdog watchdog;

        public PrintResultHandler(final ExecuteWatchdog watchdog)
        {
            this.watchdog = watchdog;
        }

        public PrintResultHandler(final int exitValue) {
            super.onProcessComplete(exitValue); // call to a
        }

    }

    @Test
    public void testTutorialExample() throws Exception {

        final long printJobTimeout = 15000;
        final boolean printInBackground = false;
        final File pdfFile = new File("/Documents and Settings/foo.pdf");

        PrintResultHandler printResult;

        try {
            // printing takes around 10 seconds
            System.out.println("[main] Preparing print job ...");
            printResult = print(pdfFile, printJobTimeout, printInBackground); // calls a
            System.out.println("[main] Successfully sent the print job ...");
        }
        catch (final Exception e) {
            e.printStackTrace();
            fail("[main] Printing of the following document failed : " + pdfFile.getAbsolutePath());
            throw e;
        }

        // come back to check the print result
        System.out.println("[main] Test is exiting but waiting for the print job to finish...");
        printResult.waitFor();  // calls b
        System.out.println("[main] The print job has finished ...");
    }

    public PrintResultHandler print(final File file, final long printJobTimeout, final boolean printInBackground)
            throws IOException { // called from above

        int exitValue;
        ExecuteWatchdog watchdog = null;
        PrintResultHandler resultHandler;
        ...
        // pass a "ExecuteResultHandler" when doing background printing
        if (printInBackground) {
            System.out.println("[print] Executing non-blocking print job  ...");
            resultHandler = new PrintResultHandler(watchdog);
            executor.execute(commandLine, resultHandler);
        }
        else {
            System.out.println("[print] Executing blocking print job  ...");
            exitValue = executor.execute(commandLine);
            resulthandler = new PrintResultHandler(exitValue); // calls a
        }

        return resultHandler;
    }

}
