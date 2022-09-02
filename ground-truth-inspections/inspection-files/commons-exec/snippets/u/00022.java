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

    public String getExecutable() { // indirectly called from DefaultExecutor.execute()
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

}
