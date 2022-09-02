public interface Executor {
    /**
     * Checks whether {@code exitValue} signals a failure. If no
     * exit values are set than the default conventions of the OS is
     * used. e.g. most OS regard an exit code of '0' as successful
     * execution and everything else as failure.
     *
     * @param exitValue the exit value (return code) to be checked
     * @return {@code true} if {@code exitValue} signals a failure
     */
    boolean isFailure(final int exitValue); // a

    /**
     * Get the working directory of the created process.
     *
     * @return the working directory
     */
    File getWorkingDirectory(); // b
}

public class DefaultExecutor implements Executor {{

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // only implementation of a

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
    public void testExecute() throws Exception {
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl);
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue)); // call to a
        assertEquals(new File("."), exec.getWorkingDirectory()); // call to b
    }
}
