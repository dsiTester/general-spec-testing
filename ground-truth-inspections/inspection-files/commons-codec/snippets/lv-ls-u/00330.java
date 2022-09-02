public class PercentCodec {
    private byte[] doEncode(final byte[] bytes, final int expectedLength, final boolean willEncode) { // definition of a
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
                    final int u = Utils.digit16(bytes[++i]); // throws exception in validated case
                    final int l = Utils.digit16(bytes[++i]);
                    buffer.put((byte) ((u << 4) + l));
                } catch (final ArrayIndexOutOfBoundsException e) {
                    throw new DecoderException("Invalid percent decoding: ", e); // throws exception in invalidated case (expected)
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
    public byte[] encode(final byte[] bytes) throws EncoderException { // called from tests
        if (bytes == null) {
            return null;
        }

        final int expectedEncodingBytes = expectedEncodingBytes(bytes);
        final boolean willEncode = expectedEncodingBytes != bytes.length;
        if (willEncode || (plusForSpace && containsSpace(bytes))) {
            return doEncode(bytes, expectedEncodingBytes, willEncode); // call to a
        }
        return bytes;
    }
}

public class Utils {
    static int digit16(final byte b) throws DecoderException {
        final int i = Character.digit((char) b, RADIX);
        if (i == -1) {
            throw new DecoderException("Invalid URL encoding: not a valid digit (radix " + RADIX + "): " + b); // this exception is thrown in the validated test
        }
        return i;
    }

}

public class PercentCodecTest {
    @Test
    public void testUnsafeCharEncodeDecode() throws Exception { // validated test
        final PercentCodec percentCodec = new PercentCodec();
        final String input = "\u03B1\u03B2\u03B3\u03B4\u03B5\u03B6% ";
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // calls a
        final String encodedS = new String(encoded, "UTF-8");
        final byte[] decoded = percentCodec.decode(encoded); // call to b; throws exception
        final String decodedS = new String(decoded, "UTF-8");
        assertEquals("Basic PercentCodec unsafe char encoding test", "%CE%B1%CE%B2%CE%B3%CE%B4%CE%B5%CE%B6%25 ", encodedS);
        assertEquals("Basic PercentCodec unsafe char decoding test", input, decodedS);
    }

    @Test
    public void testDecodeInvalidEncodedResultDecoding() throws Exception { // invalidated test
        final String inputS = "\u03B1\u03B2";
        final PercentCodec percentCodec = new PercentCodec();
        final byte[] encoded = percentCodec.encode(inputS.getBytes("UTF-8")); // calls a
        try {
            percentCodec.decode(Arrays.copyOf(encoded, encoded.length-1)); // call to b;exclude one byte
        } catch (final Exception e) {
            assertTrue(DecoderException.class.isInstance(e) &&
                ArrayIndexOutOfBoundsException.class.isInstance(e.getCause()));
        }
    }

    @Test
    public void testPercentEncoderDecoderWithPlusForSpace() throws Exception { // unknown test
        final String input = "a b c d";
        final PercentCodec percentCodec = new PercentCodec(null, true);
        final byte[] encoded = percentCodec.encode(input.getBytes(StandardCharsets.UTF_8)); // calls a
        final String encodedS = new String(encoded, "UTF-8");
        assertEquals("PercentCodec plus for space encoding test", "a+b+c+d", encodedS); // this assertion fails
        final byte[] decode = percentCodec.decode(encoded); // call to b
        assertEquals("PercentCodec plus for space decoding test", new String(decode, "UTF-8"), input);
    }
}
