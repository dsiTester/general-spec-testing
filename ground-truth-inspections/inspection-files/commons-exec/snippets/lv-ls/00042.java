public class CommandLine {
    /**
     * Cleans the executable string. The argument is trimmed and '/' and '\\' are
     * replaced with the platform specific file separator char
     *
     * @param dirtyExecutable the executable
     * @return the platform-specific executable string
     */
    private String toCleanExecutable(final String dirtyExecutable) { // definition of a
        if (dirtyExecutable == null) {
            throw new IllegalArgumentException("Executable can not be null");
        }
        if (dirtyExecutable.trim().isEmpty()) {
            throw new IllegalArgumentException("Executable can not be empty");
        }
        return StringUtils.fixFileSeparatorChar(dirtyExecutable);
    }

    /**
     * Set the substitutionMap to expand variables in the
     * command line.
     *
     * @param substitutionMap the map
     */
    public void setSubstitutionMap(final Map<String, ?> substitutionMap) { // definiton of b
        this.substitutionMap = substitutionMap;
    }

    /**
     * Create a command line without any arguments.
     *
     * @param  executable the executable file
     */
    public CommandLine(final File executable) { // called from test in validating test
        this.isFile=true;
        this.executable=toCleanExecutable(executable.getAbsolutePath()); // call to a
    }

    public CommandLine(final String executable) { // called from below in invalidating test
        this.isFile=false;
        this.executable=toCleanExecutable(executable); // call to a
    }

    public static CommandLine parse(final String line, final Map<String, ?> substitutionMap) { // called from invalidated test

        if (line == null) {
            throw new IllegalArgumentException("Command line can not be null");
        }
        if (line.trim().isEmpty()) {
            throw new IllegalArgumentException("Command line can not be empty");
        }
        final String[] tmp = translateCommandline(line);

        final CommandLine cl = new CommandLine(tmp[0]); // calls a
        cl.setSubstitutionMap(substitutionMap);         // call to b
        for (int i = 1; i < tmp.length; i++) {
            cl.addArgument(tmp[i]);
        }

        return cl;
    }

}

public class Exec36Test {
    @Test
    public void testExec36_2() throws Exception { // validated test

        String expected;
        ...
        CommandLine cmdl;
        final File file = new File("/Documents and Settings/myusername/Local Settings/Temp/netfx.log");
        final Map<String, File> map = new HashMap<>();
        map.put("FILE", file);

        cmdl = new CommandLine(printArgsScript); // calls a
        cmdl.setSubstitutionMap(map); // call to b
        cmdl.addArgument("dotnetfx.exe", false);
        cmdl.addArgument("/q:a", false);
        cmdl.addArgument("/c:\"install.exe /l \"\"${FILE}\"\" /q\"", false);

        final int exitValue = exec.execute(cmdl); // throws exception
        final String result = baos.toString().trim();
        assertFalse(exec.isFailure(exitValue));

        if (OS.isFamilyUnix()) {
            // the parameters fall literally apart under Windows - need to disable the check for Win32
            assertEquals(expected, result);
        }
    }

}

public class CommandLineTest {
    @Test
    public void testToString() throws Exception { // invalidated test
        CommandLine cmdl;
        final HashMap<String, String> params = new HashMap<>();

        // use no arguments
        cmdl = CommandLine.parse("AcroRd32.exe", params);
        assertEquals("[AcroRd32.exe]", cmdl.toString());

        // use an argument containing spaces
        params.put("file", "C:\\Document And Settings\\documents\\432432.pdf");
        cmdl = CommandLine.parse("AcroRd32.exe /p /h '${file}'", params);
        assertEquals("[AcroRd32.exe, /p, /h, \"C:\\Document And Settings\\documents\\432432.pdf\"]", cmdl.toString());

        // use an argument without spaces
        params.put("file", "C:\\documents\\432432.pdf");
        cmdl = CommandLine.parse("AcroRd32.exe /p /h '${file}'", params);
        assertEquals("[AcroRd32.exe, /p, /h, C:\\documents\\432432.pdf]", cmdl.toString());
    }

}
