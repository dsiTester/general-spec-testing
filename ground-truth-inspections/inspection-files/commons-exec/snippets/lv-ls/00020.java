public class CommandLine {

    /**
     * Encapsulates a command line argument.
     */
    class Argument {            // "call site" of a and b

        private final String value;
        private final boolean handleQuoting;

        private Argument(final String value, final boolean handleQuoting)
        {
            this.value = value.trim();
            this.handleQuoting = handleQuoting;
        }

        private String getValue()
        {                       // definition of a
            return value;
        }

        private boolean isHandleQuoting()
        {                       // definition of b
            return handleQuoting;
        }
    }

    public String[] getArguments() {

        Argument currArgument;
        String expandedArgument;
        final String[] result = new String[arguments.size()];

        for (int i=0; i<result.length; i++) {
            currArgument = arguments.get(i);
            expandedArgument = expandArgument(currArgument.getValue()); // call to a?
            result[i] = currArgument.isHandleQuoting() ? StringUtils.quoteArgument(expandedArgument) : expandedArgument; // call to b?
        }

        return result;
    }
}

public class DefaultExecutorTest {
    @Test
    public void testExecuteWithArg() throws Exception { // validated test
        final CommandLine cl = new CommandLine(testScript);
        cl.addArgument("BAR");
        final int exitValue = exec.execute(cl); // calls a and b

        assertEquals("FOO..BAR", baos.toString().trim());
        assertFalse(exec.isFailure(exitValue));
    }

    @Test
    public void testExecuteWithComplexArguments() throws Exception { // invalidated test
        final CommandLine cl = new CommandLine(printArgsScript);
        cl.addArgument("gdal_translate");
        cl.addArgument("HDF5:\"/home/kk/grass/data/4404.he5\"://HDFEOS/GRIDS/OMI_Column_Amount_O3/Data_Fields/ColumnAmountO3/home/kk/4.tif", false);
        final DefaultExecutor executor = new DefaultExecutor();
        final int exitValue = executor.execute(cl); // calls a and b
        assertFalse(exec.isFailure(exitValue));
     }
}
