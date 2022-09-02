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
     * Decode bytes encoded with Percent-Encoding based on RFC 3986. The reverse process is performed in order to
     * decode the encoded characters to Unicode.
     */
    @Override
    public byte[] decode(final byte[] bytes) throws DecoderException { // definition of b
        if (bytes == null) {
            return null;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(expectedDecodingBytes(bytes));
        for (int i = 0; i < bytes.length; i++) {
            final byte b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    final int u = Utils.digit16(bytes[++i]);
                    final int l = Utils.digit16(bytes[++i]);
                    buffer.put((byte) ((u << 4) + l));
                } catch (final ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid percent decoding: ", e);
                }
            } else {
                if (plusForSpace && b == '+') {
                    buffer.put((byte) ' ');
                } else {
                    buffer.put(b);
                }
            }
        }
        return buffer.array();
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
    public Object decode(final Object obj) throws DecoderException { // called from invalidated test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return decode((byte[]) obj); // call to b
        }
        throw new DecoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent decoded"); // throws expected exception here
    }


}

public class PercentCodecTest {

    @Test
    public void testDecodeInvalidEncodedResultDecoding() throws Exception { // validated test
        final String inputS = "\u03B1\u03B2";
        final PercentCodec percentCodec = new PercentCodec(); // calls a
        final byte[] encoded = percentCodec.encode(inputS.getBytes("UTF-8"));
        try {
            percentCodec.decode(Arrays.copyOf(encoded, encoded.length-1)); // call to b
        } catch (final Exception e) {
            assertTrue(DecoderException.class.isInstance(e) &&
                ArrayIndexOutOfBoundsException.class.isInstance(e.getCause()));
        }
    }

    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded); // call b
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

}
