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

public class Base32 extends BaseNCodec {
    @Override
    void decode(final byte[] input, int inPos, final int inAvail, final Context context) {
        ...
        // Two forms of EOF as far as Base32 decoder is concerned: actual
        // EOF (-1) and first time '=' character is encountered in stream.
        // This approach makes the '=' padding characters completely optional.
        if (context.eof && context.modulus > 0) { // if modulus == 0, nothing to do
            final byte[] buffer = ensureBufferSize(decodeSize, context); // calls a?
            ...
            switch (context.modulus) {
//              case 0 : // impossible, as excluded above
                case 1 : // 5 bits - either ignore entirely, or raise an exception
                    validateTrailingCharacters();
                case 2 : // 10 bits, drop 2 and output one byte
                    validateCharacter(MASK_2BITS, context); // calls b?
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 2) & MASK_8BITS);
                    break;
                case 3 : // 15 bits, drop 7 and output 1 byte, or raise an exception
                    validateTrailingCharacters();
                    // Not possible from a valid encoding but decode anyway
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 7) & MASK_8BITS);
                    break;
                case 4 : // 20 bits = 2*8 + 4
                    validateCharacter(MASK_4BITS, context); // calls b?
                    ...
                    break;
                case 5 : // 25 bits = 3*8 + 1
                    validateCharacter(MASK_1BITS, context); // calls b?
                    ...
                    break;
                case 6 : // 30 bits = 3*8 + 6, or raise an exception
                    validateTrailingCharacters(); 
                    ...
                    break;
                case 7 : // 35 bits = 4*8 +3
                    validateCharacter(MASK_3BITS, context); // calls b?
                    ...
                    break;
                default:
                    // modulus can be 0-7, and we excluded 0,1 already
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
        }
    }

    private void validateCharacter(final long emptyBitsMask, final Context context) {
        // Use the long bit work area
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) { // call to b
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 32 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
        }
    }

}

public class Base32InputStreamTest {
    @Test
    public void testAvailable() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        try (final Base32InputStream b32stream = new Base32InputStream(ins)) {
            assertEquals(1, b32stream.available());
            assertEquals(3, b32stream.skip(10));
            // End of stream reached
            assertEquals(0, b32stream.available());
            assertEquals(-1, b32stream.read());
            assertEquals(-1, b32stream.read());
            assertEquals(0, b32stream.available());
        }
    }
}
