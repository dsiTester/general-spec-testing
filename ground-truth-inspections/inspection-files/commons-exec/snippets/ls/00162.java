public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setWorkingDirectory(java.io.File)
     */
    @Override
    public void setWorkingDirectory(final File dir) { // definition of a
        this.workingDirectory = dir;
    }

    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // definition of b
        this.streamHandler = streamHandler;
    }
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteWithRedirectedStreams() throws Exception {
        if (OS.isFamilyUnix()) {
            final FileInputStream fis = new FileInputStream("./NOTICE.txt");
            final CommandLine cl = new CommandLine(redirectScript);
            final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(baos, baos, fis);
            final DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(new File(".")); // call to a
            executor.setStreamHandler(pumpStreamHandler); // call to b
            final int exitValue = executor.execute(cl);
            fis.close();
            final String result = baos.toString().trim();
            assertTrue(result, result.indexOf("Finished reading from stdin") > 0);
            assertFalse("exitValue=" + exitValue, exec.isFailure(exitValue));
        } else if (OS.isFamilyWindows()) {
            System.err
                    .println("The code samples to do that in windows look like a joke ... :-( .., no way I'm doing that");
            System.err.println("The test 'testExecuteWithRedirectedStreams' does not support the following OS : "
                    + System.getProperty("os.name"));
        } else {
            System.err.println("The test 'testExecuteWithRedirectedStreams' does not support the following OS : "
                    + System.getProperty("os.name"));
        }
    }

}
