public class DefaultExecutor implements Executor { // DSI in invalidated test claims that this is the call site of method-b.
    /**
     * Execute an internal process. If the executing thread is interrupted while waiting for the
     * child process to return the child process will be killed.
     *
     * @param command the command to execute
     * @param environment the execution environment
     * @param dir the working directory
     * @param streams process the streams (in, out, err) of the process
     * @return the exit code of the process
     * @throws IOException executing the process failed
     */
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // definition ofa

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

        try {
            streams.setProcessInputStream(process.getOutputStream());
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }

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
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException { // called from test

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        return executeInternal(command, environment, workingDirectory, streamHandler); // call to a

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
    public void testExec36_2() throws Exception { // validated test

        String expected;

        // the original command line
        // dotnetfx.exe /q:a /c:"install.exe /l ""\Documents and Settings\myusername\Local Settings\Temp\netfx.log"" /q"

        if (OS.isFamilyWindows()) {
            expected = "dotnetfx.exe\n" +
                    "/q:a\n" +
                    "/c:\"install.exe /l \"\"\\Documents and Settings\\myusername\\Local Settings\\Temp\\netfx.log\"\" /q\"";
        }
        else if (OS.isFamilyUnix()) {
            expected = "dotnetfx.exe\n" +
                    "/q:a\n" +
                    "/c:\"install.exe /l \"\"/Documents and Settings/myusername/Local Settings/Temp/netfx.log\"\" /q\"";
        }
        else {
            System.err.println("The test 'testExec36_3' does not support the following OS : " + System.getProperty("os.name"));
            return;
        }

        CommandLine cmdl;
        final File file = new File("/Documents and Settings/myusername/Local Settings/Temp/netfx.log");
        final Map<String, File> map = new HashMap<>();
        map.put("FILE", file);

        cmdl = new CommandLine(printArgsScript);
        cmdl.setSubstitutionMap(map);
        cmdl.addArgument("dotnetfx.exe", false);
        cmdl.addArgument("/q:a", false);
        cmdl.addArgument("/c:\"install.exe /l \"\"${FILE}\"\" /q\"", false);

        final int exitValue = exec.execute(cmdl); // calls a
        final String result = baos.toString().trim();
        assertFalse(exec.isFailure(exitValue)); // call to b

        if (OS.isFamilyUnix()) {
            // the parameters fall literally apart under Windows - need to disable the check for Win32
            assertEquals(expected, result); // assertion fails here
        }
    }

}

public class DefaultExecutorTest {
    /**
     * Start a asynchronous process which returns an success
     * exit value.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExecuteAsync() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(testScript);
        final DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        exec.execute(cl, resultHandler); // calls a
        resultHandler.waitFor(2000);
        assertTrue(resultHandler.hasResult());
        assertNull(resultHandler.getException());
        assertFalse(exec.isFailure(resultHandler.getExitValue())); // call to b?
        assertEquals("FOO..", baos.toString().trim());
    }

    /**
     * The simplest possible test - start a script and
     * check that the output was pumped into our
     * {@code ByteArrayOutputStream}.
     *
     * @throws Exception the test failed
     */
    @Test
    public void testExecute() throws Exception { // unknown test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a
        assertEquals("FOO..", baos.toString().trim()); // assertion fails here
        assertFalse(exec.isFailure(exitValue)); // call to b
        assertEquals(new File("."), exec.getWorkingDirectory());
    }
}
