public interface Executor {
    /**
     * Set the working directory of the created process. The
     * working directory must exist when you start the process.
     *
     * @param dir the working directory
     */
    void setWorkingDirectory(File dir); // a

}

public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setWorkingDirectory(java.io.File)
     */
    @Override
    public void setWorkingDirectory(final File dir) { // only implementation of a
        this.workingDirectory = dir;
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine, java.util.Map)
     */
    @Override
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // definition of b

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist."); // throws exception expected by validated test
        }

        return executeInternal(command, environment, workingDirectory, streamHandler);

    }

    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {       // called from test
        return execute(command, (Map<String, String>) null); // call to b
    }
}

public class DefaultExecutorTest {

    @Test(expected = IOException.class) // this expected exception was not thrown
    public void testExecuteWithInvalidWorkingDirectory() throws Exception { // validated test
        final File workingDir = new File("/foo/bar");
        final CommandLine cl = new CommandLine(testScript);
        exec.setWorkingDirectory(workingDir); // call to a

        exec.execute(cl);       // calls b
    }

    @Test
    public void testExecuteWithWorkingDirectory() throws Exception { // invalidated test
        final File workingDir = new File("./target");
        final CommandLine cl = new CommandLine(testScript);
        exec.setWorkingDirectory(workingDir); // call to a
        final int exitValue = exec.execute(cl); // calls b
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
        assertEquals(exec.getWorkingDirectory(), workingDir);
    }

}

/*
  Script ran by tests:

echo FOO.$TEST_ENV_VAR.$1
*/
