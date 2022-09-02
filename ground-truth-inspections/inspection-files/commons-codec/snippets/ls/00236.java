public class DoubleMetaphone {

    /**
     * Determines whether or not a value is of slavo-germanic origin. A value is
     * of slavo-germanic origin if it contians any of 'W', 'K', 'CZ', or 'WITZ'.
     */
    private boolean isSlavoGermanic(final String value) { // definition of a
        return value.indexOf('W') > -1 || value.indexOf('K') > -1 ||
            value.indexOf("CZ") > -1 || value.indexOf("WITZ") > -1;
    }

    /**
     * Determines whether or not the value starts with a silent letter.  It will
     * return {@code true} if the value starts with any of 'GN', 'KN',
     * 'PN', 'WR' or 'PS'.
     */
    private boolean isSilentStart(final String value) { // definition of b
        boolean result = false;
        for (final String element : SILENT_START) {
            if (value.startsWith(element)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public boolean isDoubleMetaphoneEqual(final String value1, final String value2, final boolean alternate) { // called from test
        return StringUtils.equals(doubleMetaphone(value1, alternate), doubleMetaphone(value2, alternate)); // calls a and b
    }

    public String doubleMetaphone(String value, final boolean alternate) {
        value = cleanInput(value);
        if (value == null) {
            return null;
        }

        final boolean slavoGermanic = isSlavoGermanic(value); // call to a
        int index = isSilentStart(value) ? 1 : 0;             // call to b
        ...
    }
}

public class DoubleMetaphoneTest {
    @Test
    public void testCodec184() throws Throwable {
        assertTrue(new DoubleMetaphone().isDoubleMetaphoneEqual("", "", false));
        assertTrue(new DoubleMetaphone().isDoubleMetaphoneEqual("", "", true));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("aa", "", false));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("aa", "", true));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("", "aa", false));
        assertFalse(new DoubleMetaphone().isDoubleMetaphoneEqual("", "aa", true));
      }

}
