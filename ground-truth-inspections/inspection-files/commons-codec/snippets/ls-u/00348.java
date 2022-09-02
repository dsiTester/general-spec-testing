public class PercentCodec {

    private int expectedEncodingBytes(final byte[] bytes) { // definition of a
        int byteCount = 0;
        for (final byte b : bytes) {
            byteCount += canEncode(b) ? 3: 1;
        }
        return byteCount;
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
        final int expectedEncodingBytes = expectedEncodingBytes(bytes); // call to a
        final boolean willEncode = expectedEncodingBytes != bytes.length;
        if (willEncode || (plusForSpace && containsSpace(bytes))) {
            return doEncode(bytes, expectedEncodingBytes, willEncode); // throws BufferOverflowException
        }
        return bytes;
    }

    private byte[] doEncode(final byte[] bytes, final int expectedLength, final boolean willEncode) {
        final ByteBuffer buffer = ByteBuffer.allocate(expectedLength); // DSI replacement caused this line to allocate 0 bytes
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
                    buffer.put(b); // BufferOverflowException here
                }
            }
        }
        return buffer.array();
    }

    @Override
    public byte[] decode(final byte[] bytes) throws DecoderException { // called from test
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
    public void testPercentEncoderDecoderWithNullOrEmptyInput() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true);
        assertEquals("Null input value encoding test", percentCodec.encode(null), null);
        assertEquals("Null input value decoding test", percentCodec.decode(null), null);
        final byte[] emptyInput = "".getBytes("UTF-8");
        assertEquals("Empty input value encoding test", percentCodec.encode(emptyInput), emptyInput); // calls a
        assertTrue("Empty input value decoding test", Arrays.equals(percentCodec.decode(emptyInput), emptyInput)); // calls b
    }

    @Test
    public void testPercentEncoderDecoderWithPlusForSpace() throws Exception { // unknown test
        final String input = "a b c d";
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // calls a and b
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("PercentCodec plus for space encoding test", "a+b+c+d", encodedS);
        final byte[] decode = percentCodec.decode(encoded); // calls b
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }

}
