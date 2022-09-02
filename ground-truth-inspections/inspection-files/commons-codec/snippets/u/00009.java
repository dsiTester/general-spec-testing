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

public class Soundex implements StringEncoder {
    @Override
    public String encode(final String str) { // used implementation of a
        return soundex(str); // call to b
    }

    /**
     * Retrieves the Soundex code for a given String object.
     *
     * @param str
     *                  String to encode using the Soundex algorithm
     * @return A soundex code for the String supplied
     * @throws IllegalArgumentException
     *                  if a character is not mapped
     */
    public String soundex(String str) { // definition of b
        if (str == null) {
            return null;
        }
        str = SoundexUtils.clean(str);
        if (str.isEmpty()) {
            return str;
        }
        final char out[] = {'0', '0', '0', '0'};
        int count = 0;
        final char first = str.charAt(0);
        out[count++] = first;
        char lastDigit = map(first); // previous digit
        for(int i = 1; i < str.length() && count < out.length ; i++) {
            final char ch = str.charAt(i);
            if ((this.specialCaseHW) && (ch == 'H' || ch == 'W')) { // these are ignored completely
                continue;
            }
            final char digit = map(ch);
            if (digit == SILENT_MARKER) {
                continue;
            }
            if (digit != '0' && digit != lastDigit) { // don't store vowels or repeats
                out[count++] = digit;
            }
            lastDigit = digit;
        }
        return new String(out);
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
