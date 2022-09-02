public class DefaultExecutor implements Executor {
    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // definition of a/b

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

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from Runnable called by DefaultExecutor.execute()

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
            closeProcessStreams(process);

            if (getExceptionCaught() != null) {
                throw getExceptionCaught();
            }
            ...
            if (this.isFailure(exitValue)) { // call to a
                throw new ExecuteException("Process exited with an error: " + exitValue, exitValue); // this exception was supposed to be thrown in the unknown test, but wasn't because a was replaced iwth false.
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

public class DefaultExecutorTest {
    @Test
    public void testExecute() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue)); // call to b
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

    @Test
    public void testExecuteWithError() throws Exception { // unknown verdict test
        final CommandLine cl = new CommandLine(errorTestScript);

        try{
            exec.execute(cl);   // calls a
            fail("Must throw ExecuteException");
        } catch (final ExecuteException e) {
            assertTrue(exec.isFailure(e.getExitValue())); // call to b
        }
    }

}
