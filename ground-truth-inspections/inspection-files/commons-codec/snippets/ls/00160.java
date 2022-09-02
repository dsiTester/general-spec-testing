public abstract class BaseNCodec {
    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of a
        return DEFAULT_BUFFER_SIZE;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to a
            ...
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }
}

public class Base32 extends BaseNCodec {
    /**
     * Validates whether decoding the final trailing character is possible in the context
     * of the set of possible base 32 values.
     *
     * <p>The character is valid if the lower bits within the provided mask are zero. This
     * is used to test the final trailing base-32 digit is zero in the bits that will be discarded.
     *
     * @param emptyBitsMask The mask of the lower bits that should be empty
     * @param context the context to be used
     *
     * @throws IllegalArgumentException if the bits being checked contain any non-zero value
     */
    private void validateCharacter(final long emptyBitsMask, final Context context) { // definition of b
        // Use the long bit work area
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) {
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 32 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
        }
    }

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
                ...
                case 5 : // 25 bits = 3*8 + 1
                    validateCharacter(MASK_1BITS, context); // call to b
                    ...
                    break;
                ...
                default:
                    // modulus can be 0-7, and we excluded 0,1 already
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
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
