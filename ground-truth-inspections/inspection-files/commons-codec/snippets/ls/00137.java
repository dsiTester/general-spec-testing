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

    /**
     * Returns the amount of buffered data available for reading.
     *
     * @param context the context to be used
     * @return The amount of buffered data available for reading.
     */
    int available(final Context context) {  // definition of b
        return context.buffer != null ? context.pos - context.readPos : 0;
    }

    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) {
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail); // call to b
            ...
            return len;
        }
        return context.eof ? EOF : 0;
    }

}

public class Base64 extends BaseNCodec {
    private void validateCharacter(final int emptyBitsMask, final Context context) {
        if (isStrictDecoding() && (context.ibitWorkArea & emptyBitsMask) != 0) { // call to a
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
        Assert.assertFalse(codec.isStrictDecoding()); // this is different from a
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            codec.decode(s);
        }
    }
}
