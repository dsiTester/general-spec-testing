public class DoubleMetaphone {
    public class DoubleMetaphoneResult {

        public void append(final char value) { // definition of a
            appendPrimary(value);
            appendAlternate(value);
        }

        public String getPrimary() { // definition of b
            return this.primary.toString();
        }

        public void appendPrimary(final char value) { // called by a
            if (this.primary.length() < this.maxLength) {
                this.primary.append(value);
            }
        }
    }

    public boolean isDoubleMetaphoneEqual(final String value1, final String value2) { // called from invalidated test
        return isDoubleMetaphoneEqual(value1, value2, false);
    }

    public boolean isDoubleMetaphoneEqual(final String value1, final String value2, final boolean alternate) {
        return StringUtils.equals(doubleMetaphone(value1, alternate), doubleMetaphone(value2, alternate)); // calls a and b
    }

    public String doubleMetaphone(String value, final boolean alternate) {
        ...

        final DoubleMetaphoneResult result = new DoubleMetaphoneResult(this.getMaxCodeLen());

        while (!result.isComplete() && index <= value.length() - 1) {
            switch (value.charAt(index)) {
            case 'A':
            case 'E':
            case 'I':
            case 'O':
            case 'U':
            case 'Y':
                index = handleAEIOUY(result, index); // calls a (validated test)
                break;
            ...
            case 'N':
                result.append('N'); // call to a (invalidated test)
                index = charAt(value, index + 1) == 'N' ? index + 2 : index + 1;
                break;
            case '\u00D1':
                // N with a tilde (spanish ene)
                result.append('N'); // call to a (invalidated test)
                index++;
                break;
            ...
            default:
                index++;
                break;
            }
        }

        return alternate ? result.getAlternate() : result.getPrimary(); // call to b
    }

    @Override
    public String encode(final String value) { // called from valiated test
        return doubleMetaphone(value);         // calls a and b
    }

    public String doubleMetaphone(final String value) { // called from above in validated test
        return doubleMetaphone(value, false); // calls a and b
    }

    private int handleAEIOUY(final DoubleMetaphoneResult result, final int index) { // called from doubleMetaphone() (in validated test case)
        if (index == 0) {
            result.append('A'); // call to a
        }
        return index + 1;
    }

}

public class DoubleMetaphoneTest extends StringEncoderAbstractTest<DoubleMetaphone> {
    @Test
    public void testNTilde() {  // invalidated test
        assertTrue(this.getStringEncoder().isDoubleMetaphoneEqual("\u00f1", "N")); // n-tilde
    }
}

public abstract class StringEncoderAbstractTest<T extends StringEncoder> {
    @Test
    public void testLocaleIndependence() throws Exception {
        final StringEncoder encoder = this.getStringEncoder();

        final String[] data = {"I", "i",};

        final Locale orig = Locale.getDefault();
        final Locale[] locales = {Locale.ENGLISH, new Locale("tr"), Locale.getDefault()};

        try {
            for (final String element : data) {
                String ref = null;
                for (int j = 0; j < locales.length; j++) {
                    Locale.setDefault(locales[j]);
                    if (j <= 0) {
                        ref = encoder.encode(element);
                    } else {
                        String cur = null;
                        try {
                            cur = encoder.encode(element); // calls a and b
                        } catch (final Exception e) {
                            Assert.fail(Locale.getDefault().toString() + ": " + e.getMessage());
                        }
                        Assert.assertEquals(Locale.getDefault().toString() + ": ", ref, cur); // assertion fails here
                    }
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
    }
}
