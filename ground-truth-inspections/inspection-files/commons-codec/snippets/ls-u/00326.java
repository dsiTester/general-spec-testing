public class PercentCodec {
    private boolean containsSpace(final byte[] bytes) { // definition of a
        for (final byte b : bytes) {
            if (b == ' ') {
                return true;
            }
        }
        return false;
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

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from tests
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[]) obj); // calls a
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
    }

    @Override
    public byte[] encode(final byte[] bytes) throws EncoderException {
        ...
        final boolean willEncode = expectedEncodingBytes != bytes.length;
        if (willEncode || (plusForSpace && containsSpace(bytes))) { // call to a
            return doEncode(bytes, expectedEncodingBytes, willEncode);
        }
        return bytes;
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
    public byte[] decode(final byte[] bytes) throws DecoderException { // called from unknown test; called from above in invalidated test
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
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // calls a
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded); // calls b
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

    @Test
    public void testPercentEncoderDecoderWithPlusForSpace() throws Exception { // unknown verdict test
        final String input = "a b c d";
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // calls a
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("PercentCodec plus for space encoding test", "a+b+c+d", encodedS); // assertion fails here
        final byte[] decode = percentCodec.decode(encoded); // calls b
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }

}
