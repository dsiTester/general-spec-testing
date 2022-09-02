public class CommandLine {
    /**
     * Set the substitutionMap to expand variables in the
     * command line.
     *
     * @param substitutionMap the map
     */
    public void setSubstitutionMap(final Map<String, ?> substitutionMap) { // definition of a
        this.substitutionMap = substitutionMap;
    }

    /**
     * Returns the executable.
     *
     * @return The executable
     */
    public String getExecutable() { // definition of b
        // Expand the executable and replace '/' and '\\' with the platform
        // specific file separator char. This is safe here since we know
        // that this is a platform specific command.
        return StringUtils.fixFileSeparatorChar(expandArgument(executable));
    }

    public static CommandLine parse(final String line, final Map<String, ?> substitutionMap) { // called from tests

        ...
        final String[] tmp = translateCommandline(line);

        final CommandLine cl = new CommandLine(tmp[0]);
        cl.setSubstitutionMap(substitutionMap); // call to a
        for (int i = 1; i < tmp.length; i++) {
            cl.addArgument(tmp[i]);
        }

        return cl;
    }

    public String[] toStrings() { // called from CommandLine.toString()
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable(); // call to b
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
    }

    private String expandArgument(final String argument) { // called from b
        final StringBuffer stringBuffer = StringUtils.stringSubstitution(argument, this.getSubstitutionMap(), true); // accesses CommandLine.substitutionMap
        return stringBuffer.toString();
    }
}

public class CommandLineTest {
    /**
     * A command line parsing puzzle from Tino Schoellhorn - ImageMagix expects
     * a "500x>" parameter (including quotes) and it is simply not possible to
     * do that without adding a space, e.g. "500x> ".
     */
    @Test
    public void testParseComplexCommandLine1() {
        final HashMap<String, String> substitutionMap =
            new HashMap<>();
        substitutionMap.put("in", "source.jpg");
        substitutionMap.put("out", "target.jpg");
        final CommandLine cmdl = CommandLine.parse("cmd /C convert ${in} -resize \"\'500x> \'\" ${out}", substitutionMap); // call to a
        assertEquals("[cmd, /C, convert, source.jpg, -resize, \"500x> \", target.jpg]", cmdl.toString()); // calls b; fails here
    }

    /**
     * Another  command line parsing puzzle from Kai Hu - as
     * far as I understand it there is no way to express that
     * in a one-line command string.
     */
    @Test
    public void testParseComplexCommandLine2() {

        final String commandline = "./script/jrake cruise:publish_installers "
            + "INSTALLER_VERSION=unstable_2_1 "
            + "INSTALLER_PATH=\"/var/lib/ cruise-agent/installers\" "
            + "INSTALLER_DOWNLOAD_SERVER=\'something\' "
            + "WITHOUT_HELP_DOC=true";

        final CommandLine cmdl = CommandLine.parse(commandline);
        final String[] args = cmdl.getArguments(); // call to b
        assertEquals(args[0], "cruise:publish_installers");
        assertEquals(args[1], "INSTALLER_VERSION=unstable_2_1");
        // assertEquals(args[2], "INSTALLER_PATH=\"/var/lib/ cruise-agent/installers\"");
        // assertEquals(args[3], "INSTALLER_DOWNLOAD_SERVER='something'");
        assertEquals(args[4], "WITHOUT_HELP_DOC=true");
    }
}
