public abstract class BaseNCodec {
    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of a
        return DEFAULT_BUFFER_SIZE;
    }

    /**
     * Returns true if decoding behavior is strict. Decoding will raise an {@link IllegalArgumentException} if trailing
     * bits are not part of a valid encoding.
     *
     * <p>
     * The default is false for lenient decoding. Decoding will compose trailing bits into 8-bit bytes and discard the
     * remainder.
     * </p>
     *
     * @return true if using strict decoding
     * @since 1.15
     */
    public boolean isStrictDecoding() { // definition of b
        return decodingPolicy == CodecPolicy.STRICT;
    }
}

public class Base16 extends BaseNCodec {
    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // transitively called from test
        if (context.eof || length < 0) {
            context.eof = true;
            if (context.ibitWorkArea != 0) {
                validateTrailingCharacter(); // calls b
            }
            return;
        }
        ...
        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // calls a
        ...
    }

    private void validateTrailingCharacter() {
        if (isStrictDecoding()) { // call to b
            throw new IllegalArgumentException("Strict decoding: Last encoded character is a valid base 16 alphabet" +
                    "character but not a possible encoding. " +
                    "Decoding requires at least two characters to create one byte.");
        }
    }
}


public class Base16Test {
    @Test
    public void testLenientDecoding() {
        final String encoded = "aabbccdde";  // Note the trailing `e` which does not make up a hex-pair and so is only 1/2 byte

        final Base16 b16 = new Base16(true, CodecPolicy.LENIENT);
        assertEquals(CodecPolicy.LENIENT, b16.getCodecPolicy());

        final byte[] decoded = b16.decode(StringUtils.getBytesUtf8(encoded));
        assertArrayEquals(new byte[] {(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd}, decoded);
    }
}
