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
            return encode((byte[]) obj);
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
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
