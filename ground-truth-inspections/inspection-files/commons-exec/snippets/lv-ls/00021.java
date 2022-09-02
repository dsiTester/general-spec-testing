public class CommandLine {

    /**
     * Expand variables in a command line argument.
     *
     * @param argument the argument
     * @return the expanded string
     */
    private String expandArgument(final String argument) { // definition of a
        final StringBuffer stringBuffer = StringUtils.stringSubstitution(argument, this.getSubstitutionMap(), true);
        return stringBuffer.toString();
    }

    /**
     * Returns the expanded and quoted command line arguments.
     *
     * @return The quoted arguments
     */
    public String[] getArguments() {

        Argument currArgument;
        String expandedArgument;
        final String[] result = new String[arguments.size()];

        for (int i=0; i<result.length; i++) {
            currArgument = arguments.get(i);
            expandedArgument = expandArgument(currArgument.getValue());
            result[i] = currArgument.isHandleQuoting() ? StringUtils.quoteArgument(expandedArgument) : expandedArgument;
        }

        return result;
    }

    public String[] toStrings() { // called from DefaultExecutor.execute()
        final String[] result = new String[arguments.size() + 1];
        // String[] a = getArguments();
        // result[0] = this.getExecutable();
        // System.arraycopy(a, 0, result, 1, result.length-1);
        // NOTE: to call b before a, replace the below with the above
        result[0] = this.getExecutable(); // calls a
        System.arraycopy(getArguments(), 0, result, 1, result.length-1); // call to b
        return result;
    }

    public String getExecutable() { // called from above
        // Expand the executable and replace '/' and '\\' with the platform
        // specific file separator char. This is safe here since we know
        // that this is a platform specific command.
        return StringUtils.fixFileSeparatorChar(expandArgument(executable)); // call to a
    }

}

public class DefaultExecutorTest {
    @Test
    public void testExecute() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        final int exitValue = exec.execute(cl); // calls a and b; exception thrown here
        assertEquals("FOO..", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
        assertEquals(new File("."), exec.getWorkingDirectory());
    }

    /**
     * Try to start an non-existing application which should result
     * in an exception.
     */
    @Test(expected = IOException.class)
    public void testExecuteNonExistingApplication() throws Exception {
        final CommandLine cl = new CommandLine(nonExistingTestScript);
        final DefaultExecutor executor = new DefaultExecutor();

        executor.execute(cl);   // calls a and b; exception thrown but expected
    }
}
