public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine, java.util.Map)
     */
    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // only implementation of a

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // calls b

    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from above

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
            closeProcessStreams(process); // call to b

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

public interface Executor {
    /**
     * Methods for starting synchronous execution.
     *
     * @param command the command to execute
     * @param environment The environment for the new process. If null, the
     *          environment of the current process is used.
     * @return process exit value
     * @throws ExecuteException execution of subprocess failed or the
     *          subprocess returned a exit value indicating a failure
     *          {@link Executor#setExitValue(int)}.
     */
    int execute(CommandLine command, Map<String, String> environment)
        throws ExecuteException, IOException; // a
}

public class DefaultExecutorTest {

    @Test
    public void testAddEnvironmentVariables() throws Exception {
        final Map<String, String> myEnvVars = new HashMap<>(EnvironmentUtils.getProcEnvironment());
        myEnvVars.put("NEW_VAR","NEW_VAL");
        exec.execute(new CommandLine(environmentSript), myEnvVars); // call to a
        final String environment = baos.toString().trim();
        assertTrue("Expecting NEW_VAR in "+environment,environment.indexOf("NEW_VAR") >= 0);
        assertTrue("Expecting NEW_VAL in "+environment,environment.indexOf("NEW_VAL") >= 0);
    }
}
