public class PercentCodec {
    /**
     * Adds the byte array into a BitSet for faster lookup
     *
     * @param alwaysEncodeCharsArray
     */
    private void insertAlwaysEncodeChars(final byte[] alwaysEncodeCharsArray) { // definition of a
        if (alwaysEncodeCharsArray != null) {
            for (final byte b : alwaysEncodeCharsArray) {
                insertAlwaysEncodeChar(b);
            }
        }
        insertAlwaysEncodeChar(ESCAPE_CHAR);
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

    public PercentCodec(final byte[] alwaysEncodeChars, final boolean plusForSpace) { // called from test
        this.plusForSpace = plusForSpace;
        insertAlwaysEncodeChars(alwaysEncodeChars); // calls a
    }

    @Override
    public Object decode(final Object obj) throws DecoderException { // called from invalidated test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return decode((byte[]) obj); // calls b
        }
        throw new DecoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent decoded");
    }

    @Override
    public byte[] decode(final byte[] bytes) throws DecoderException {
        if (bytes == null) {
            return null;
        }

        final ByteBuffer buffer = ByteBuffer.allocate(expectedDecodingBytes(bytes)); // call to b
        ...
        return buffer.array();
    }

}

public class PercentCodecTest {
    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded); // calls b
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

    @Test
    public void testConfigurablePercentEncoder() throws Exception { // unknown test
        final String input = "abc123_-.*\u03B1\u03B2";
        final PercentCodec percentCodec = new PercentCodec("abcdef".getBytes("UTF-8"), false); // calls a
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("Configurable PercentCodec encoding test", "%61%62%63123_-.*%CE%B1%CE%B2", encodedS); // assertion fails here
        final byte[] decoded = percentCodec.decode(encoded); // call to b
        assertEquals("Configurable PercentCodec decoding test", new String(decoded, "UTF-8"), input);
    }
}
