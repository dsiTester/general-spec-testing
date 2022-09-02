public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine)
     */
    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {       // only implementation of a
        return execute(command, (Map<String, String>) null); // calls b
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine, java.util.Map)
     */
    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // definition of b

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler);

    }

}

public interface Executor {
    /**
     * Methods for starting synchronous execution. The child process inherits
     * all environment variables of the parent process.
     *
     * @param command the command to execute
     * @return process exit value
     * @throws ExecuteException execution of subprocess failed or the
     *          subprocess returned a exit value indicating a failure
     *          {@link Executor#setExitValue(int)}.
     */
    int execute(CommandLine command)
        throws ExecuteException, IOException; // a
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteWithArg() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        cl.addArgument("BAR");
        final int exitValue = exec.execute(cl); // call to a

        assertEquals("FOO..BAR", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
    }

}
