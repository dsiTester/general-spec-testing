public interface Executor {
    /**
     * Set the working directory of the created process. The
     * working directory must exist when you start the process.
     *
     * @param dir the working directory
     */
    void setWorkingDirectory(File dir); // a

    /**
     * Get the working directory of the created process.
     *
     * @return the working directory
     */
    File getWorkingDirectory(); // b
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
     * @see org.apache.commons.exec.Executor#getWorkingDirectory()
     */
    @Override
    public File getWorkingDirectory() { // only implementation of b
        return workingDirectory;
    }
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteWithWorkingDirectory() throws Exception {
        final File workingDir = new File("./target");
        final CommandLine cl = new CommandLine(testScript);
        exec.setWorkingDirectory(workingDir); // call to a
        final int exitValue = exec.execute(cl);
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
        assertEquals(exec.getWorkingDirectory(), workingDir); // call to b
    }

}

/*
  Script ran by test:

echo FOO.$TEST_ENV_VAR.$1
*/
