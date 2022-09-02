public abstract class BaseNCodec {
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
    public boolean isStrictDecoding() { // definition of a
        return decodingPolicy == CodecPolicy.STRICT;
    }
}

public class BaseNCodecOutputStream {
    public boolean isStrictDecoding() {
        return baseNCodec.isStrictDecoding(); // call to a
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
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) { // calls a???
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
            final byte[] buffer = ensureBufferSize(decodeSize, context);

            ...
            switch (context.modulus) {
//              case 0 : // impossible, as excluded above
                case 1 : // 5 bits - either ignore entirely, or raise an exception
                    validateTrailingCharacters();
                case 2 : // 10 bits, drop 2 and output one byte
                    validateCharacter(MASK_2BITS, context); // call to b
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 2) & MASK_8BITS);
                    break;
                case 3 : // 15 bits, drop 7 and output 1 byte, or raise an exception
                    validateTrailingCharacters();
                    // Not possible from a valid encoding but decode anyway
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 7) & MASK_8BITS);
                    break;
                case 4 : // 20 bits = 2*8 + 4
                    validateCharacter(MASK_4BITS, context); // call to b
                    ...
                    break;
                case 5 : // 25 bits = 3*8 + 1
                    validateCharacter(MASK_1BITS, context); // call to b
                    ...
                    break;
                case 6 : // 30 bits = 3*8 + 6, or raise an exception
                    validateTrailingCharacters();
                    ...
                    break;
                case 7 : // 35 bits = 4*8 +3
                    validateCharacter(MASK_3BITS, context); // call to b
                    ...
                    break;
                default:
                    // modulus can be 0-7, and we excluded 0,1 already
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
        }
    }


}

public class Base32OutputStreamTest {
    @Test
    public void testStrictDecoding() throws Exception {
        for (final String s : Base32Test.BASE32_IMPOSSIBLE_CASES) {
            final byte[] encoded = StringUtils.getBytesUtf8(s);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Base32OutputStream out = new Base32OutputStream(bout, false);
            // Default is lenient decoding; it should not throw
            assertFalse(out.isStrictDecoding()); // calls a
            out.write(encoded);
            out.close(); // calls b
            assertTrue(bout.size() > 0);

            // Strict decoding should throw
            bout = new ByteArrayOutputStream();
            out = new Base32OutputStream(bout, false, 0, null, CodecPolicy.STRICT);
            assertTrue(out.isStrictDecoding()); // calls a
            try {
                out.write(encoded);
                out.close(); // calls b
                fail();
            } catch (final IllegalArgumentException ex) {
                // expected
            }
        }
    }
}
