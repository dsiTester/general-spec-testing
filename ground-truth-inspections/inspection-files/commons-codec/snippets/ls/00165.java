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
    public boolean isStrictDecoding() { // definition of a/b; not defined in Base32
        return decodingPolicy == CodecPolicy.STRICT;
    }
}

public class BaseNCodecOutputStream {
    public boolean isStrictDecoding() {
        return baseNCodec.isStrictDecoding(); // call to "a"
    }
}

public class Base32 extends BaseNCodec {
    private void validateCharacter(final long emptyBitsMask, final Context context) {
        // Use the long bit work area
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) { // call to "b"
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 32 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
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
