public class DoubleMetaphone {
    @Override
    public String encode(final String value) { // called from test
        return doubleMetaphone(value);         // calls a
    }

    public String doubleMetaphone(final String value) {
        return doubleMetaphone(value, false);           // call to a
    }

    /**
     * Encode a value with Double Metaphone, optionally using the alternate encoding.
     *
     * @param value String to encode
     * @param alternate use alternate encode
     * @return an encoded string
     */
    public String doubleMetaphone(String value, final boolean alternate) { // definition of a
        value = cleanInput(value); // call to b
        ...                     // redacting due to a being a lengthy method
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
