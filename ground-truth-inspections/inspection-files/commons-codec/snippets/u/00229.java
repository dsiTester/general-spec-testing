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
        return doubleMetaphone(value, false);           // call to b
    }

    /**
     * Encode a value with Double Metaphone, optionally using the alternate encoding.
     *
     * @param value String to encode
     * @param alternate use alternate encode
     * @return an encoded string
     */
    public String doubleMetaphone(String value, final boolean alternate) { // definition of b
        value = cleanInput(value);
        ...                     // redacting due to b being a lengthy method
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
