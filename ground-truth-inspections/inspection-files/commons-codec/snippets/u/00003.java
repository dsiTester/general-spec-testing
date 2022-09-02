public interface StringEncoder {
    /**
     * Encodes a String and returns a String.
     *
     * @param source
     *            the String to encode
     * @return the encoded String
     * @throws EncoderException
     *             thrown if there is an error condition during the encoding process.
     */
    String encode(String source) throws EncoderException; // a
}

public class DoubleMetaphone {

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

    @Override
    public String encode(final String value) { // used implementation of a
        return doubleMetaphone(value); // calls b
    }

    public String doubleMetaphone(final String value) {
        return doubleMetaphone(value, false); // calls b
    }

    public String doubleMetaphone(String value, final boolean alternate) {
        value = cleanInput(value); // call to b
        if (value == null) {
            return null;
        }
        ...
    }

}

public abstract class StringEncoderAbstractTest {
    @Test
    public void testEncodeNull() throws Exception {
        final StringEncoder encoder = this.getStringEncoder();
        try {
            encoder.encode(null); // call to a
            // NOTE: NO fail() HERE!!!
        } catch (final EncoderException ee) {
            // An exception should be thrown
        }
    }
}

public class DoubleMetaphoneTest extends StringEncoderAbstractTest<DoubleMetaphone> {
    ...
}
