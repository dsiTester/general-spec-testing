public class DefaultExecutor implements Executor {
    /**
     * Creates a process that runs a command.
     *
     * @param command
     *            the command to run
     * @param env
     *            the environment for the command
     * @param dir
     *            the working directory for the command
     * @return the process started
     * @throws IOException
     *             forwarded from the particular launcher used
     */
    protected Process launch(final CommandLine command, final Map<String, String> env,
            final File dir) throws IOException { // definition of a

        if (this.launcher == null) {
            throw new IllegalStateException("CommandLauncher can not be null");
        }

        if (dir != null && !dir.exists()) {
            throw new IOException(dir + " doesn't exist.");
        }
        return this.launcher.exec(command, env, dir);
    }

    /**
     * @see org.apache.commons.exec.Executor#getWorkingDirectory()
     */
    @Override
    public File getWorkingDirectory() { // only implementation of b
        return workingDirectory;
    }

    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from Runnable called by DefaultExecutor.executeInternal()

        ...
        try {
            process = this.launch(command, environment, dir); // call to a
        }
        catch(final IOException e) {
            if(watchdog != null) {
                watchdog.failedToStart(e);
            }
            throw e;
        }
        ...
        try {
            streams.setProcessInputStream(process.getOutputStream()); // NullPointerException here
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
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
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory()); // call to b
    }
}
