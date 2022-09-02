public interface CommandLauncher {
    /**
     * Launches the given command in a new process, in the given working
     * directory.
     *
     * @param cmd
     *            The command to execute
     * @param env
     *            The environment for the new process. If null, the environment
     *            of the current process is used.
     * @param workingDir
     *            The directory to start the command in. If null, the current
     *            directory is used
     *
     * @return the newly created process
     * @throws IOException
     *             if trying to change directory
     */
    Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException; // a

    /**
     * Checks whether {@code exitValue} signals a failure on the current
     * system (OS specific).
     * <p>
     * <b>Note</b> that this method relies on the conventions of the OS, it
     * will return false results if the application you are running doesn't
     * follow these conventions. One notable exception is the Java VM provided
     * by HP for OpenVMS - it will return 0 if successful (like on any other
     * platform), but this signals a failure on OpenVMS. So if you execute a new
     * Java VM on OpenVMS, you cannot trust this method.
     * </p>
     *
     * @param exitValue the exit value (return code) to be checked
     * @return {@code true} if {@code exitValue} signals a failure
     */
    boolean isFailure(final int exitValue); // b
}

public class Java13CommandLauncher extends CommandLauncherImpl {
    /**
     * Launches the given command in a new process, in the given working
     * directory
     *
     * @param cmd
     *            the command line to execute as an array of strings
     * @param env
     *            the environment to set as an array of strings
     * @param workingDir
     *            the working directory where the command should run
     * @throws IOException
     *             probably forwarded from Runtime#exec
     */
    @Override
    public Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException { // used implementation of a

        final String[] envVars = EnvironmentUtils.toStrings(env);

        return Runtime.getRuntime().exec(cmd.toStrings(),
                envVars, workingDir);
    }
}

public abstract class CommandLauncherImpl implements CommandLauncher {
    /** @see org.apache.commons.exec.launcher.CommandLauncher#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue)
    {                           // used implementation of b
        // non zero exit value signals failure
        return exitValue != 0;
    }
}

public class DefaultExecutor implements Executor {
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException { // called from above

        final Process process;
        exceptionCaught = null;

        try {
            process = this.launch(command, environment, dir); // call to b
        }
        catch(final IOException e) {
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
            if (this.isFailure(exitValue)) { // calls b
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

    protected Process launch(final CommandLine command, final Map<String, String> env,
            final File dir) throws IOException { // called from DefaultExecutor.executeInternal()

        if (this.launcher == null) {
            throw new IllegalStateException("CommandLauncher can not be null");
        }

        if (dir != null && !dir.exists()) {
            throw new IOException(dir + " doesn't exist.");
        }
        return this.launcher.exec(command, env, dir); // call to a
    }

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) { // called from DefaultExecutor.executeInternal()

        if (this.exitValues == null) {
            return false;
        }
        if (this.exitValues.length == 0) {
            return this.launcher.isFailure(exitValue); // call to b
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
    public void testAddEnvironmentVariables() throws Exception {
        final Map<String, String> myEnvVars = new HashMap<>(EnvironmentUtils.getProcEnvironment());
        myEnvVars.put("NEW_VAR","NEW_VAL");
        exec.execute(new CommandLine(environmentSript), myEnvVars); // calls a and b
        final String environment = baos.toString().trim();
        assertTrue("Expecting NEW_VAR in "+environment,environment.indexOf("NEW_VAR") >= 0);
        assertTrue("Expecting NEW_VAL in "+environment,environment.indexOf("NEW_VAL") >= 0);
    }
}
