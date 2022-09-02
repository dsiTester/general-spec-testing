public class CommandLine {

    /**
     * Returns the executable.
     *
     * @return The executable
     */
    public String getExecutable() { // definition of a
        // Expand the executable and replace '/' and '\\' with the platform
        // specific file separator char. This is safe here since we know
        // that this is a platform specific command.
        return StringUtils.fixFileSeparatorChar(expandArgument(executable)); // calls b
    }

    private String expandArgument(final String argument) { // called from a
        final StringBuffer stringBuffer = StringUtils.stringSubstitution(argument, this.getSubstitutionMap(), true); // call to b
        return stringBuffer.toString();
    }

    /**
     * @return the substitution map
     */
    public Map<String, ?> getSubstitutionMap() { // definition of b
        return substitutionMap;
    }

    public String[] toStrings() { // indirectly called from DefaultExecutor.execute()
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable(); // call to a
        system.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
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
