public class PercentCodec {
    public PercentCodec(final byte[] alwaysEncodeChars, final boolean plusForSpace) {
        this.plusForSpace = plusForSpace;
        insertAlwaysEncodeChars(alwaysEncodeChars); // call to a
    }

    private void insertAlwaysEncodeChars(final byte[] alwaysEncodeCharsArray) { // definition of a
        if (alwaysEncodeCharsArray != null) {
            for (final byte b : alwaysEncodeCharsArray) {
                insertAlwaysEncodeChar(b);
            }
        }
        insertAlwaysEncodeChar(ESCAPE_CHAR); // call to b
    }

    /**
     * Inserts a single character into a BitSet and maintains the min and max of the characters of the
     * {@code BitSet alwaysEncodeChars} in order to avoid look-ups when a byte is out of this range.
     *
     * @param b the byte that is candidate for min and max limit
     */
    private void insertAlwaysEncodeChar(final byte b) { // definition of b
        this.alwaysEncodeChars.set(b);
        if (b < alwaysEncodeCharsMin) {
            alwaysEncodeCharsMin = b;
        }
        if (b > alwaysEncodeCharsMax) {
            alwaysEncodeCharsMax = b;
        }
    }
}

public class PercentCodecTest {
    @Test
    public void testPercentEncoderDecoderWithPlusForSpace() throws Exception {
        final String input = "a b c d";
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a and b
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("PercentCodec plus for space encoding test", "a+b+c+d", encodedS);
        final byte[] decode = percentCodec.decode(encoded);
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }
}
