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

    @Override
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {                                    // called from test
        return execute(command, (Map<String, String>) null); // call to a
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

public class Exec36Test {
    @Test
    public void testExec36_1() throws Exception { // validated test

        if (OS.isFamilyUnix()) {

            CommandLine cmdl;

            /**
             * ./script/jrake cruise:publish_installers INSTALLER_VERSION=unstable_2_1 \
             *     INSTALLER_PATH="/var/lib/ cruise-agent/installers" INSTALLER_DOWNLOAD_SERVER='something' WITHOUT_HELP_DOC=true
             */

            final String expected = "./script/jrake\n" +
                    "cruise:publish_installers\n" +
                    "INSTALLER_VERSION=unstable_2_1\n" +
                    "INSTALLER_PATH=\"/var/lib/ cruise-agent/installers\"\n" +
                    "INSTALLER_DOWNLOAD_SERVER='something'\n" +
                    "WITHOUT_HELP_DOC=true";

            cmdl = new CommandLine(printArgsScript);
            ...
            final int exitValue = exec.execute(cmdl); // calls a
            final String result = baos.toString().trim();
            assertFalse(exec.isFailure(exitValue)); // call to b
            assertEquals(expected, result); // assertion fails here
        }
        else {
            System.err.println("The test 'testExec36_1' does not support the following OS : " + System.getProperty("os.name"));
        }
    }
}

public class LogOutputStreamTest {
    @Test
    public void testStdout() throws Exception { // invalidated test
        this.systemOut = new SystemLogOutputStream(1);
        this.exec.setStreamHandler(new PumpStreamHandler(systemOut, systemOut));

        final CommandLine cl = new CommandLine(environmentScript);
        final int exitValue = exec.execute(cl); // calls a
        assertFalse(exec.isFailure(exitValue)); // call to b
    }

}

public class DefaultExecutorTest {
    @Test
    public void testExecute() throws Exception { // unknown test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a
        assertEquals("FOO..", baos.toString().trim()); // assertion fails here
        assertFalse(exec.isFailure(exitValue));        // call to b
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

}
