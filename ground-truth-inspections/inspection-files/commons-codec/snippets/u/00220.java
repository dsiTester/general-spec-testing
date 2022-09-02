public class DoubleMetaphone {
    public class DoubleMetaphoneResult {

        public void append(final String value) { // definition of a
            appendPrimary(value);
            appendalternate(value); // call to b
        }

        public void appendAlternate(final String value) { // definition of b
            final int addChars = this.maxLength - this.alternate.length();
            if (value.length() <= addChars) {
                this.alternate.append(value);
            } else {
                this.alternate.append(value.substring(0, addChars));
            }
        }
    }

    private int handleX(final String value, final DoubleMetaphoneResult result, int index) {
        if (index == 0) {
            ...
        } else {
            if (!((index == value.length() - 1) &&
                  (contains(value, index - 3, 3, "IAU", "EAU") ||
                   contains(value, index - 2, 2, "AU", "OU")))) {
                //-- French e.g. breaux --//
                result.append("KS"); // call to a
            }
            index = contains(value, index + 1, 1, "C", "X") ? index + 2 : index + 1;
        }
        return index;
    }

}

public class DoubleMetaphoneTest {
    @Test
    public void testDoubleMetaphone() {
        assertDoubleMetaphone("TSTN", "testing");
        assertDoubleMetaphone("0", "The");
        assertDoubleMetaphone("KK", "quick");
        assertDoubleMetaphone("PRN", "brown");
        assertDoubleMetaphone("FKS", "fox");
        assertDoubleMetaphone("JMPT", "jumped");
        assertDoubleMetaphone("AFR", "over");
        assertDoubleMetaphone("0", "the");
        assertDoubleMetaphone("LS", "lazy");
        assertDoubleMetaphone("TKS", "dogs");
        assertDoubleMetaphone("MKFR", "MacCafferey");
        assertDoubleMetaphone("STFN", "Stephan");
        assertDoubleMetaphone("KSSK", "Kuczewski");
        assertDoubleMetaphone("MKLL", "McClelland");
        assertDoubleMetaphone("SNHS", "san jose");
        assertDoubleMetaphone("SNFP", "xenophobia");

        ...
    }

    private void assertDoubleMetaphone(final String expected, final String source) {
        assertEquals(expected, this.getStringEncoder().encode(source));
        try {
            assertEquals(expected, this.getStringEncoder().encode((Object) source));
        } catch (final EncoderException e) {
            fail("Unexpected expection: " + e);
        }
        assertEquals(expected, this.getStringEncoder().doubleMetaphone(source));
        assertEquals(expected, this.getStringEncoder().doubleMetaphone(source, false));
    }

}
