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
     * Encode a value with Double Metaphone, optionally using the alternate encoding.
     *
     * @param value String to encode
     * @param alternate use alternate encode
     * @return an encoded string
     */
    public String doubleMetaphone(String value, final boolean alternate) { // definition of b
        value = cleanInput(value);
        if (value == null) {
            return null;
        }

        final boolean slavoGermanic = isSlavoGermanic(value);
        int index = isSilentStart(value) ? 1 : 0;

        final DoubleMetaphoneResult result = new DoubleMetaphoneResult(this.getMaxCodeLen());

        while (!result.isComplete() && index <= value.length() - 1) {
            switch (value.charAt(index)) {
                ...
            }
        }

        return alternate ? result.getAlternate() : result.getPrimary();
    }

    @Override
    public String encode(final String value) { // used implementation of a
        return doubleMetaphone(value); // calls b
    }

    public String doubleMetaphone(final String value) {
        return doubleMetaphone(value, false); // call to b
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
