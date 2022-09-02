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

    private byte[] doEncode(final byte[] bytes, final int expectedLength, final boolean willEncode) { // definition of b
        final ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        for (final byte b : bytes) {
            if (willEncode && canEncode(b)) { // the outcome of canEncode() will depend on a call to a
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

    public PercentCodec(final byte[] alwaysEncodeChars, final boolean plusForSpace) { // called from test
        this.plusForSpace = plusForSpace;
        insertAlwaysEncodeChars(alwaysEncodeChars); // calls a
    }

    @Override
    public byte[] encode(final byte[] bytes) throws EncoderException { // called from test
        ...
        if (willEncode || (plusForSpace && containsSpace(bytes))) {
            return doEncode(bytes, expectedEncodingBytes, willEncode); // call to b
        }
        return bytes;
    }

}

public class PercentCodecTest {
    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // calls b
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded);
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

}
