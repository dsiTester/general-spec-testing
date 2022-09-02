public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine)
     */
    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {       // definition of a
        return execute(command, (Map<String, String>) null); // calls b
    }

    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // called from a

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // call to b

    }

    /**
     * Execute an internal process. If the executing thread is interrupted while waiting for the
     * child process to return the child process will be killed.
     *
     * @param command the command to execute
     * @param environment the execution environment
     * @param dir the working directory
     * @param streams process the streams (in, out, err) of the process
     * @return the exit code of the process
     * @throws IOException executing the process failed
     */
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // definition of b, shortened

        ...
        try {
            process = this.launch(command, environment, dir);
        }
        ...

        streams.start();

        try {

            ...

            int exitValue = Executor.INVALID_EXITVALUE;
            ...
            closeProcessStreams(process); // call to b

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

public class Exec57Test extends AbstractExecTest {
    @Test(timeout = TEST_TIMEOUT)
    public void testExecutionOfDetachedProcess() throws IOException {

        if (!OS.isFamilyUnix()) {
            testNotSupportedForCurrentOperatingSystem();
            return;
        }

        final CommandLine cmdLine = new CommandLine("sh").addArgument("-c").addArgument("./src/test/scripts/issues/exec-57-detached.sh", false);
        final DefaultExecutor executor = new DefaultExecutor();
        final PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(System.out, System.err);

        executor.setStreamHandler(pumpStreamHandler);

        executor.execute(cmdLine); // calls a
    }
}
