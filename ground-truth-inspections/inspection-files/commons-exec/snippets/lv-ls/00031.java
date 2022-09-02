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
     * Returns the command line as an array of strings.
     *
     * @return The command line as an string array
     */
    public String[] toStrings() { // definition of b
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable();
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
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

    @Override
    public String toString() {  // called from tests
        return "[" + StringUtils.toString(toStrings(), ", ") + "]"; // calls b
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

    @Test
    public void testToString() throws Exception {
        CommandLine cmdl;
        final HashMap<String, String> params = new HashMap<>();

        // use no arguments
        cmdl = CommandLine.parse("AcroRd32.exe", params); // calls a
        assertEquals("[AcroRd32.exe]", cmdl.toString());  // calls b

        // use an argument containing spaces
        params.put("file", "C:\\Document And Settings\\documents\\432432.pdf");
        cmdl = CommandLine.parse("AcroRd32.exe /p /h '${file}'", params); // calls a
        assertEquals("[AcroRd32.exe, /p, /h, \"C:\\Document And Settings\\documents\\432432.pdf\"]", cmdl.toString()); // calls b

        // use an argument without spaces
        params.put("file", "C:\\documents\\432432.pdf");
        cmdl = CommandLine.parse("AcroRd32.exe /p /h '${file}'", params);
        assertEquals("[AcroRd32.exe, /p, /h, C:\\documents\\432432.pdf]", cmdl.toString());
    }

}
