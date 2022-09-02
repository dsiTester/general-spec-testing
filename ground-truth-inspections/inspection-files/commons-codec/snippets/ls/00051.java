public class BaseNCodecInputStream extends FilterInputStream {
    /**
     * {@inheritDoc}
     *
     * @return Always returns {@code false}
     */
    @Override
    public boolean markSupported() { // definition of a
        return false; // not an easy job to support marks
    }

    // b is defined in FilterInputStream, which is a third party class.
}

public class Base16InputStreamTest {
    /**
     * Tests markSupported.
     *
     * @throws IOException for some failure scenarios.
     */
    @Test
    public void testMarkSupported() throws IOException {
        final byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        try (final Base16InputStream in = new Base16InputStream(bin, true)) {
            // Always returns false for now.
            assertFalse("Base16InputStream.markSupported() is false", in.markSupported()); // call to a
        } // implicit call to b due to try with resources
    }

}
