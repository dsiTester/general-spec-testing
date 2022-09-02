public class PercentCodec {
    /**
     * Inserts a single character into a BitSet and maintains the min and max of the characters of the
     * {@code BitSet alwaysEncodeChars} in order to avoid look-ups when a byte is out of this range.
     *
     * @param b the byte that is candidate for min and max limit
     */
    private void insertAlwaysEncodeChar(final byte b) { // definition of a
        this.alwaysEncodeChars.set(b);
        if (b < alwaysEncodeCharsMin) {
            alwaysEncodeCharsMin = b;
        }
        if (b > alwaysEncodeCharsMax) {
            alwaysEncodeCharsMax = b;
        }
    }

    /**
     * Percent-Encoding based on RFC 3986. The non US-ASCII characters are encoded, as well as the
     * US-ASCII characters that are configured to be always encoded.
     */
    @Override
    public byte[] encode(final byte[] bytes) throws EncoderException { // definition of b
        if (bytes == null) {
            return null;
        }

        final int expectedEncodingBytes = expectedEncodingBytes(bytes);
        final boolean willEncode = expectedEncodingBytes != bytes.length;
        if (willEncode || (plusForSpace && containsSpace(bytes))) {
            return doEncode(bytes, expectedEncodingBytes, willEncode);
        }
        return bytes;
    }

    public PercentCodec() {     // called from validated test
        this.plusForSpace = false;
        insertAlwaysEncodeChar(ESCAPE_CHAR); // call to a
    }

    public PercentCodec(final byte[] alwaysEncodeChars, final boolean plusForSpace) { // called from invalidated test
        this.plusForSpace = plusForSpace;
        insertAlwaysEncodeChars(alwaysEncodeChars); // calls a
    }

    private void insertAlwaysEncodeChars(final byte[] alwaysEncodeCharsArray) { // called from above
        if (alwaysEncodeCharsArray != null) {
            for (final byte b : alwaysEncodeCharsArray) {
                insertAlwaysEncodeChar(b); // this also seems like a call to a, but most likely wasn't called from this test
            }
        }
        insertAlwaysEncodeChar(ESCAPE_CHAR); // call to a
    }

    private byte[] doEncode(final byte[] bytes, final int expectedLength, final boolean willEncode) { // called by b
        final ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        for (final byte b : bytes) {
            if (willEncode && canEncode(b)) {
                ...
                final char hex1 = Utils.hexDigit(bb >> 4); // throws a DecoderException if a is delayed
                ...
            } ...
        }
        return buffer.array();
    }
}

public class PercentCodecTest {

    @Test
    public void testUnsafeCharEncodeDecode() throws Exception { // validated test
        final PercentCodec percentCodec = new PercentCodec();   // calls a
        final String input = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6% ";
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // call to b
        final String encodedS = new String(encoded, "UTF-8");
        final byte[] decoded = percentCodec.decode(encoded);
        final String decodedS = new String(decoded, "UTF-8");
        assertEquals("Basic PercentCodec unsafe char encoding test", "%CE%B1%CE%B2%CE%B3%CE%B4%CE%B5%CE%B6%25 ", encodedS);
        assertEquals("Basic PercentCodec unsafe char decoding test", input, decodedS);
    }

    @Test
    public void testPercentEncoderDecoderWithPlusForSpace() throws Exception { // invalidated test
        final String input = "a b c d";
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // call to b
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("PercentCodec plus for space encoding test", "a+b+c+d", encodedS);
        final byte[] decode = percentCodec.decode(encoded);
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }

}
