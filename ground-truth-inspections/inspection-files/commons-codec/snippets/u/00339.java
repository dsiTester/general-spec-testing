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
            return doEncode(bytes, expectedEncodingBytes, willEncode);
        }
        return bytes;
    }

    /**
     * Decodes a byte[] Object, whose bytes are encoded with Percent-Encoding.
     *
     * @param obj the object to decode
     * @return the decoding result byte[] as Object
     * @throws DecoderException if the object is not a byte array
     */
    @Override
    public Object decode(final Object obj) throws DecoderException { // definition of b
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return decode((byte[]) obj);
        }
        throw new DecoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent decoded");
    }

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[]) obj); // call to a
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
    }

}

public class PercentCodecTest {
    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception {
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // call to a
        final String encodedS = new String((byte[]) encoded, "UTF-8"); // NullPointerException here
        final Object decoded = percentCodec.decode(encoded); // call to b
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }
}
