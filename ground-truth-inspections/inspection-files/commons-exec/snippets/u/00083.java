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

        return executeInternal(command, environment, workingDirectory, streamHandler); // calls b

    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from above

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
            closeProcessStreams(process);

            if (getExceptionCaught() != null) { // call to b
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

    /**
     * Get the first IOException being thrown.
     *
     * @return the first IOException being caught
     */
    private IOException getExceptionCaught() { // definition of b
        return this.exceptionCaught;
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

        executor.execute(cmdLine); // call to a
    }
}
