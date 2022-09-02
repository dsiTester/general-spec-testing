public class DefaultExecutor {
    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine, java.util.Map)
     */
    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // definition of a

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler);

    }

    /**
     * @see org.apache.commons.exec.Executor#getWorkingDirectory()
     */
    @Override
    public File getWorkingDirectory() { // definition of b
        return workingDirectory;
    }

    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {       // called from test
        return execute(command, (Map<String, String>) null); // call to a
    }
}

public interface Executor {
    /**
     * Get the working directory of the created process.
     *
     * @return the working directory
     */
    File getWorkingDirectory(); // b
}

public class DefaultExecutorTest {
    @Test
    public void testExecute() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a
        assertEquals("FOO..", baos.toString().trim()); // assertion fails here
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory()); // call to b
    }
}
