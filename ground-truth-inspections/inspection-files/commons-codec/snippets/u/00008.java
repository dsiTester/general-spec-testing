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

public class RefinedSoundex implements StringEncoder {
    @Override
    public String encode(final String str) { // used implementation of a
        return soundex(str); // call to b
    }

    /**
     * Retrieves the Refined Soundex code for a given String object.
     *
     * @param str
     *                  String to encode using the Refined Soundex algorithm
     * @return A soundex code for the String supplied
     */
    public String soundex(String str) { // definition of b
        if (str == null) {
            return null;
        }
        str = SoundexUtils.clean(str);
        if (str.isEmpty()) {
            return str;
        }

        final StringBuilder sBuf = new StringBuilder();
        sBuf.append(str.charAt(0));

        char last, current;
        last = '*';

        for (int i = 0; i < str.length(); i++) {

            current = getMappingCode(str.charAt(i));
            if (current == last) {
                continue;
            }
            if (current != 0) {
                sBuf.append(current);
            }

            last = current;

        }

        return sBuf.toString();
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

public class RefinedSoundexTest extends StringEncoderAbstractTest<RefinedSoundex> {
    ...
}
