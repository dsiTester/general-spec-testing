public class Soundex implements StringEncoder {
    /**
     * Encodes a String using the soundex algorithm.
     *
     * @param str
     *                  A String object to encode
     * @return A Soundex code corresponding to the String supplied
     * @throws IllegalArgumentException
     *                  if a character is not mapped
     */
    @Override
    public String encode(final String str) { // definition of a
        return soundex(str);                 // call to b
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

public class SoundexTest extends StringEncoderAbstractTest<Soundex> {
    @Test
    public void testEncodeIgnoreTrimmable() {
        Assert.assertEquals("W252", this.getStringEncoder().encode(" \t\n\r Washington \t\n\r ")); // call to a
    }
}
