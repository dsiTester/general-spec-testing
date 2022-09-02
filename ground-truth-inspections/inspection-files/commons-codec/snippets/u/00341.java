public class PercentCodec {
    /**
     * Percent-Encoding based on RFC 3986. The non US-ASCII characters are encoded, as well as the
     * US-ASCII characters that are configured to be always encoded.
     */
    @Override
    public byte[] encode(final byte[] bytes) throws EncoderException { // definition of a
        if (bytes == null) {
            return null;
        }

        final int expectedEncodingBytes = expectedEncodingBytes(bytes);
        final boolean willEncode = expectedEncodingBytes != bytes.length;
        if (willEncode || (plusForSpace && containsSpace(bytes))) {
            return doEncode(bytes, expectedEncodingBytes, willEncode); // call to b
        }
        return bytes;
    }

    private byte[] doEncode(final byte[] bytes, final int expectedLength, final boolean willEncode) { // definition of b
        final ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        for (final byte b : bytes) {
            if (willEncode && canEncode(b)) {
                byte bb = b;
                if (bb < 0) {
                    bb = (byte) (256 + bb);
                }
                final char hex1 = Utils.hexDigit(bb >> 4);
                final char hex2 = Utils.hexDigit(bb);
                buffer.put(ESCAPE_CHAR);
                buffer.put((byte) hex1);
                buffer.put((byte) hex2);
            } else {
                if (plusForSpace && b == ' ') {
                    buffer.put((byte) '+');
                } else {
                    buffer.put(b);
                }
            }
        }
        return buffer.array();
    }
}

public class PercentCodecTest {
    @Test
    public void testConfigurablePercentEncoder() throws Exception {
        final String input = "abc123_-.*\u03B1\u03B2";
        final PercentCodec percentCodec = new PercentCodec("abcdef".getBytes("UTF-8"), false);
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // call to a
        final String encodedS = new String(encoded, "UTF-8"); // NullPointerException here
        assertEquals("Configurable PercentCodec encoding test", "%61%62%63123_-.*%CE%B1%CE%B2", encodedS);
        final byte[] decoded = percentCodec.decode(encoded);
        assertEquals("Configurable PercentCodec decoding test", new String(decoded, "UTF-8"), input);
    }
}
