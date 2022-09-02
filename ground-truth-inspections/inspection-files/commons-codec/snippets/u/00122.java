public abstract class BaseNCodec {
    /**
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    @Override
    public byte[] decode(final byte[] pArray) { // definition of a; not defined in Base64
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls b
        ...
        return result;
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
    public boolean isStrictDecoding() { // definition of b; not defined in Base64
        return decodingPolicy == CodecPolicy.STRICT;
    }

}

public class Base64 extends BaseNCodec {
    @Override
    void decode(final byte[] in, int inPos, final int inAvail, final Context context) {
        ...
        if (context.eof && context.modulus != 0) {
            final byte[] buffer = ensureBufferSize(decodeSize, context);

            // We have some spare bits remaining
            // Output all whole multiples of 8 bits and ignore the rest
            switch (context.modulus) {
//              case 0 : // impossible, as excluded above
                case 1 : // 6 bits - either ignore entirely, or raise an exception
                    validateTrailingCharacter();
                    break;
                case 2 : // 12 bits = 8 + 4
                    validateCharacter(MASK_4BITS, context); // calls b
                    ...
                    break;
                case 3 : // 18 bits = 8 + 8 + 2
                    validateCharacter(MASK_2BITS, context); // calls b
                    ...
                    break;
                default:
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
        }
    }

    private void validateCharacter(final int emptyBitsMask, final Context context) {
        if (isStrictDecoding() && (context.ibitWorkArea & emptyBitsMask) != 0) { // call to b
            throw new IllegalArgumentException(
                                               "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                                               "base 64 alphabet but not a possible encoding. " +
                                               "Expected the discarded bits from the character to be zero.");
        }
    }
}

public class BCodec {
    @Override
    protected byte[] doDecoding(final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes);
    }

}

public class BCodecTest {
    @Test
    public void testBase64ImpossibleSamplesDefault() throws DecoderException {
        final BCodec codec = new BCodec();
        // Default encoding is lenient
        Assert.assertFalse(codec.isStrictDecoding());
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            codec.decode(s);
        }
    }
}
