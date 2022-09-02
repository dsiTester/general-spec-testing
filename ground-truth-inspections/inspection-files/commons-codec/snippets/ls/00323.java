public class PercentCodec {
    private boolean containsSpace(final byte[] bytes) { // definition of a
        for (final byte b : bytes) {
            if (b == ' ') {
                return true;
            }
        }
        return false;
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
    public Object encode(final Object obj) throws EncoderException { // called from invalidated test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[]) obj); // calls a
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
    }

    @Override
    public byte[] encode(final byte[] bytes) throws EncoderException { // called from above, also from unknown test
        ...
        final boolean willEncode = expectedEncodingBytes != bytes.length;
        if (willEncode || (plusForSpace && containsSpace(bytes))) { // call to a
            return doEncode(bytes, expectedEncodingBytes, willEncode);
        }
        return bytes;
    }

    @Override
    public Object decode(final Object obj) throws DecoderException { // called from tests
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
    public void testSafeCharEncodeDecodeObject() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8));
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded); // call to b
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

    @Test
    public void testPercentEncoderDecoderWithPlusForSpace() throws Exception { // unknown
        final String input = "a b c d";
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // calls a
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("PercentCodec plus for space encoding test", "a+b+c+d", encodedS);
        final byte[] decode = percentCodec.decode(encoded); // call to b
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }

}
