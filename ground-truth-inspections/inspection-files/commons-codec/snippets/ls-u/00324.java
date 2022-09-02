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

}

public class PercentCodecTest {
    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // calls a
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded); // call to b
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
        final byte[] decode = percentCodec.decode(encoded); // call to b
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }

}
