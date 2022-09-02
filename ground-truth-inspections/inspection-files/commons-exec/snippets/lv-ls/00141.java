public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    @Override
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) { // definition of a
        this.streamHandler = streamHandler;
    }

    /**
     * Creates a process that runs a command.
     *
     * @param command
     *            the command to run
     * @param env
     *            the environment for the command
     * @param dir
     *            the working directory for the command
     * @return the process started
     * @throws IOException
     *             forwarded from the particular launcher used
     */
    protected Process launch(final CommandLine command, final Map<String, String> env,
            final File dir) throws IOException { // definition of b

        if (this.launcher == null) {
            throw new IllegalStateException("CommandLauncher can not be null");
        }

        if (dir != null && !dir.exists()) {
            throw new IOException(dir + " doesn't exist.");
        }
        return this.launcher.exec(command, env, dir);
    }

    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {       // called from test
        return execute(command, (Map<String, String>) null); // calls b
    }

    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // called from above

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // calls b

    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from above

        ...
        try {
            process = this.launch(command, environment, dir); // call to b
        }
        catch(final IOException e) {
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

        try {
            streams.setProcessInputStream(process.getOutputStream()); // streams is DefaultExecutor.streamHandler this try block sets the link between the streams of the two processes
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        } ...

        streams.start();

        try {

            ...
            int exitValue = Executor.INVALID_EXITVALUE;

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

public interface Executor {
}

public class DefaultExecutorTest {
    /**
     * Start a process with redirected streams - stdin of the newly
     * created process is connected to a FileInputStream whereas
     * the "redirect" script reads all lines from stdin and prints
     * them on stdout. Furthermore the script prints a status
     * message on stderr.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExecuteWithRedirectedStreams() throws Exception { // validated test
        if (OS.isFamilyUnix()) {
            final FileInputStream fis = new FileInputStream("./NOTICE.txt");
            final CommandLine cl = new CommandLine(redirectScript);
            final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(baos, baos, fis);
            final DefaultExecutor executor = new DefaultExecutor();
            executor.setWorkingDirectory(new File("."));
            executor.setStreamHandler(pumpStreamHandler); // call to a
            final int exitValue = executor.execute(cl);   // calls b
            fis.close();
            final String result = baos.toString().trim();
            assertTrue(result, result.indexOf("Finished reading from stdin") > 0); // assertion fails here
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

    @Test
    public void testExecuteWithNullOutErr() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(testScript);
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(null, null);
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler); // call to a
        final int exitValue = executor.execute(cl);   // calls b
        assertFalse(exec.isFailure(exitValue));
    }

}
