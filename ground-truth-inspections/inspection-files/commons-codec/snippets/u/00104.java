public class BaseNCodecOutputStream extends FilterOutputStream {
    /**
     * Returns true if decoding behavior is strict. Decoding will raise an
     * {@link IllegalArgumentException} if trailing bits are not part of a valid encoding.
     *
     * <p>The default is false for lenient encoding. Decoding will compose trailing bits
     * into 8-bit bytes and discard the remainder.
     *
     * @return true if using strict decoding
     * @since 1.15
     */
    public boolean isStrictDecoding() { // definition of a; a is not defined in Base32OutputStream
        return baseNCodec.isStrictDecoding();
    }

    // method-b is defined in FilterOutputStream, a third party class
}

public class Base32OutputStreamTest {
    @Test
    public void testStrictDecoding() throws Exception {
        for (final String s : Base32Test.BASE32_IMPOSSIBLE_CASES) {
            final byte[] encoded = StringUtils.getBytesUtf8(s);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Base32OutputStream out = new Base32OutputStream(bout, false);
            // Default is lenient decoding; it should not throw
            assertFalse(out.isStrictDecoding()); // call to a
            out.write(encoded); // call to b
            out.close();
            assertTrue(bout.size() > 0);

            // Strict decoding should throw
            bout = new ByteArrayOutputStream();
            out = new Base32OutputStream(bout, false, 0, null, CodecPolicy.STRICT);
            assertTrue(out.isStrictDecoding()); // call to a; assertion failed here
            try {
                out.write(encoded); // call to b
                out.close();
                fail();
            } catch (final IllegalArgumentException ex) {
                // expected
            }
        }
    }
}
