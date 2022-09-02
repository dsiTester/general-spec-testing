public class BaseNCodecInputStream extends FilterInputStream {
    /**
     * {@inheritDoc}
     *
     * @return Always returns {@code false}
     */
    @Override
    public boolean markSupported() { // definition of a; not defined in Base32InputStream
        return false; // not an easy job to support marks
    }

    // b is defined in FilterInputStream, which is a third party class.
}

public class Base32InputStreamTest {
    @Test
    public void testMarkSupported() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        try (final Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 })) {
            // Always returns false for now.
            assertFalse("Base32InputStream.markSupported() is false", in.markSupported()); // call to a
        } // b is called implicitly via try-with-resources
    }

}
