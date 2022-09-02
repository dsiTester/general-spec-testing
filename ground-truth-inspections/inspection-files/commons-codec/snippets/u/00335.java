public class PercentCodec {
    /**
     * Encodes an object into using the Percent-Encoding. Only byte[] objects are accepted.
     *
     * @param obj the object to encode
     * @return the encoding result byte[] as Object
     * @throws EncoderException if the object is not a byte array
     */
    @Override
    public Object encode(final Object obj) throws EncoderException { // definition of a
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[]) obj); // call to b
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
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
}

public class PercentCodecTest {
    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception {
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // call to a
        final String encodedS = new String((byte[]) encoded, "UTF-8"); // NullPointerException here
        final Object decoded = percentCodec.decode(encoded);
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }
}
