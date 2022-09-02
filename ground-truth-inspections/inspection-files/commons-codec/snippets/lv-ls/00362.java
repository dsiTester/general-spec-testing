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

    public PercentCodec(final byte[] alwaysEncodeChars, final boolean plusForSpace) { // called from test
        this.plusForSpace = plusForSpace;
        insertAlwaysEncodeChars(alwaysEncodeChars); // calls a
    }

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from invalidated test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[]) obj); // call to b
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
    }

}

public class PercentCodecTest {
    @Test
    public void testConfigurablePercentEncoder() throws Exception { // validated test
        final String input = "abc123_-.*\u03B1\u03B2";
        final PercentCodec percentCodec = new PercentCodec("abcdef".getBytes("UTF-8"), false);
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // call to b
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("Configurable PercentCodec encoding test", "%61%62%63123_-.*%CE%B1%CE%B2", encodedS);
        final byte[] decoded = percentCodec.decode(encoded);
        assertEquals("Configurable PercentCodec decoding test", new String(decoded, "UTF-8"), input);
    }

    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // call to b
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded);
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

}
