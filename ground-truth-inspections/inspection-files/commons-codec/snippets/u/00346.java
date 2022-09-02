public class PercentCodec {

    private int expectedEncodingBytes(final byte[] bytes) { // definition of a
        int byteCount = 0;
        for (final byte b : bytes) {
            byteCount += canEncode(b) ? 3: 1;
        }
        return byteCount;
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

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[]) obj); // calls a
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
    }

    @Override
    public byte[] encode(final byte[] bytes) throws EncoderException { // definition of a
        ...
        final int expectedEncodingBytes = expectedEncodingBytes(bytes); // call to a
        final boolean willEncode = expectedEncodingBytes != bytes.length;
        if (willEncode || (plusForSpace && containsSpace(bytes))) {
            return doEncode(bytes, expectedEncodingBytes, willEncode);
        }
        return bytes;
    }

    private byte[] doEncode(final byte[] bytes, final int expectedLength, final boolean willEncode) {
        final ByteBuffer buffer = ByteBuffer.allocate(expectedLength); // DSI replacement caused this line to allocate 0 bytes
        for (final byte b : bytes) {
            if (willEncode && canEncode(b)) {
                ...
            } else {
                if (plusForSpace && b == ' ') {
                    buffer.put((byte) '+');
                } else {
                    buffer.put(b); // BufferOverflowException here
                }
            }
        }
        return buffer.array();
    }

    @Override
    public Object decode(final Object obj) throws DecoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return decode((byte[]) obj); // call to b
        }
        throw new DecoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent decoded");
    }

}

public class PercentCodecTest {
    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception {
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // calls a
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded); // calls b
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }
}
