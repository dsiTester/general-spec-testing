public interface Executor {
    /**
     * Set the working directory of the created process. The
     * working directory must exist when you start the process.
     *
     * @param dir the working directory
     */
    void setWorkingDirectory(File dir); // a

    /**
     * Checks whether {@code exitValue} signals a failure. If no
     * exit values are set than the default conventions of the OS is
     * used. e.g. most OS regard an exit code of '0' as successful
     * execution and everything else as failure.
     *
     * @param exitValue the exit value (return code) to be checked
     * @return {@code true} if {@code exitValue} signals a failure
     */
    boolean isFailure(final int exitValue); // b
}

public class DefaultExecutor implements Executor {
    /**
     * @see org.apache.commons.exec.Executor#setWorkingDirectory(java.io.File)
     */
    @Override
    public void setWorkingDirectory(final File dir) { // only implementation of a
        this.workingDirectory = dir;
    }

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // only implementation of b

        if (this.exitValues == null) {
            return false;
        }
        if (this.exitValues.length == 0) {
            return this.launcher.isFailure(exitValue);
        }
        for (final int exitValue2 : this.exitValues) {
            if (exitValue2 == exitValue) {
                return false;
            }
        }
        return true;
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
        assertFalse(exec.isFailure(exitValue)); // call to b
        assertEquals(exec.getWorkingDirectory(), workingDir);
    }

}

/*
  Script ran by test:

echo FOO.$TEST_ENV_VAR.$1
*/
