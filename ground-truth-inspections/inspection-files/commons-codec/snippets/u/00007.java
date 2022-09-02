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

public class Nysiis implements StringEncoder {
    @Override
    public String encode(final String str) { // used implementation of a
        return this.nysiis(str); // call to b
    }

    /**
     * Retrieves the NYSIIS code for a given String object.
     *
     * @param str
     *            String to encode using the NYSIIS algorithm
     * @return A NYSIIS code for the String supplied
     */
    public String nysiis(String str) { // definition of b, shortened
        if (str == null) {
            return null;
        }

        // Use the same clean rules as Soundex
        str = SoundexUtils.clean(str);

        if (str.isEmpty()) {
            return str;
        }

        // Translate first characters of name:
        // MAC -> MCC, KN -> NN, K -> C, PH | PF -> FF, SCH -> SSS
        str = PAT_MAC.matcher(str).replaceFirst("MCC");
        ...

        // Translate last characters of name:
        // EE -> Y, IE -> Y, DT | RT | RD | NT | ND -> D
        str = PAT_EE_IE.matcher(str).replaceFirst("Y");
        str = PAT_DT_ETC.matcher(str).replaceFirst("D");

        // First character of key = first character of name.
        final StringBuilder key = new StringBuilder(str.length());
        key.append(str.charAt(0));

        // Transcode remaining characters, incrementing by one character each time
        final char[] chars = str.toCharArray();
        final int len = chars.length;

        for (int i = 1; i < len; i++) {
            final char next = i < len - 1 ? chars[i + 1] : SPACE;
            final char aNext = i < len - 2 ? chars[i + 2] : SPACE;
            final char[] transcoded = transcodeRemaining(chars[i - 1], chars[i], next, aNext);
            System.arraycopy(transcoded, 0, chars, i, transcoded.length);

            // only append the current char to the key if it is different from the last one
            if (chars[i] != chars[i - 1]) {
                key.append(chars[i]);
            }
        }

        if (key.length() > 1) {
           ...
        }

        final String string = key.toString();
        return this.isStrict() ? string.substring(0, Math.min(TRUE_LENGTH, string.length())) : string;
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

public class NysiisTest extends StringEncoderAbstractTest<Nysiis> {
    ...
}
