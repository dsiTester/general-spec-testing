public class DoubleMetaphone {
    @Override
    public String encode(final String value) { // called from test
        return doubleMetaphone(value);         // call to a
    }

    /**
     * Encode a value with Double Metaphone.
     *
     * @param value String to encode
     * @return an encoded string
     */
    public String doubleMetaphone(final String value) { // definition of a
        return doubleMetaphone(value, false);
    }

    public String doubleMetaphone(String value, final boolean alternate) {
        value = cleanInput(value); // call to b
        ...
    }

    /**
     * Cleans the input.
     */
    private String cleanInput(String input) { // definition of b
        if (input == null) {
            return null;
        }
        input = input.trim();
        if (input.isEmpty()) {
            return null;
        }
        return input.toUpperCase(java.util.Locale.ENGLISH);
    }
}

public abstract class StringEncoderAbstractTest<T extends StringEncoder> { // DoubleMetaphoneTest extends this class, and doesn't implement the test
    @Test
    public void testEncodeNull() throws Exception {
        final StringEncoder encoder = this.getStringEncoder();
        try {
            encoder.encode(null); // calls a
        } catch (final EncoderException ee) {
            // An exception should be thrown
        }
    }
}
