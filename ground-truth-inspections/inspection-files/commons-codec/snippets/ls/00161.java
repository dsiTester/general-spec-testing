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
    public boolean isStrictDecoding() { // definition of b; not defined in Base64
        return decodingPolicy == CodecPolicy.STRICT;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to a
            context.pos = 0;
            context.readPos = 0;

            // Overflow-conscious:
            // x + y > z  ==  x + y - z > 0
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }
}

public class Base64 {
    private void validateCharacter(final int emptyBitsMask, final Context context) {
        if (isStrictDecoding() && (context.ibitWorkArea & emptyBitsMask) != 0) { // call to b
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 64 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
        }
    }

}

public class BCodecTest {
    @Test
    public void testBase64ImpossibleSamplesDefault() throws DecoderException {
        final BCodec codec = new BCodec();
        // Default encoding is lenient
        Assert.assertFalse(codec.isStrictDecoding());
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            codec.decode(s); // calls a and b
        }
    }

}
