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

    private int expectedDecodingBytes(final byte[] bytes) { // definition of b
        int byteCount = 0;
        for (int i = 0; i < bytes.length; ) {
            final byte b = bytes[i];
            i += b == ESCAPE_CHAR ? 3: 1;
            byteCount++;
        }
        return byteCount;
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

    @Override
    public byte[] decode(final byte[] bytes) throws DecoderException { // called from tests
        if (bytes == null) {
            return null;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(expectedDecodingBytes(bytes)); // call to b
        for (int i = 0; i < bytes.length; i++) {
            final byte b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    final int u = Utils.digit16(bytes[++i]); // DecoderException thrown here
                    final int l = Utils.digit16(bytes[++i]);
                    buffer.put((byte) ((u << 4) + l));
                } catch (final ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid percent decoding: ", e);
                }
            } ...
        }
        ...
        return buffer.array();
    }
}

public class Utils {
    static int digit16(final byte b) throws DecoderException {
        final int i = Character.digit((char) b, RADIX);
        if (i == -1) {
            throw new DecoderException("Invalid URL encoding: not a valid digit (radix " + RADIX + "): " + b); // exception thrown
        }
        return i;
    }
}

public class PercentCodecTest {

    @Test
    public void testUnsafeCharEncodeDecode() throws Exception { // validated test
        final PercentCodec percentCodec = new PercentCodec();   // calls a
        final String input = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6% ";
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String(encoded, "UTF-8");
        final byte[] decoded = percentCodec.decode(encoded); // calls b
        final String decodedS = new String(decoded, "UTF-8");
        assertEquals("Basic PercentCodec unsafe char encoding test", "%CE%B1%CE%B2%CE%B3%CE%B4%CE%B5%CE%B6%25 ", encodedS);
        assertEquals("Basic PercentCodec unsafe char decoding test", input, decodedS);
    }

    @Test
    public void testPercentEncoderDecoderWithPlusForSpace() throws Exception { // invalidated test
        final String input = "a b c d";
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("PercentCodec plus for space encoding test", "a+b+c+d", encodedS);
        final byte[] decode = percentCodec.decode(encoded); // calls b
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }

}
