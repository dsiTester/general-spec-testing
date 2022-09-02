public class BaseNCodecInputStream extends FilterInputStream {
    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if the provided skip length is negative
     * @since 1.7
     */
    @Override
    public long skip(final long n) throws IOException { // definition of a
        if (n < 0) {
            throw new IllegalArgumentException("Negative skip length: " + n);
        }

        // skip in chunks of 512 bytes
        final byte[] b = new byte[512];
        long todo = n;

        while (todo > 0) {
            int len = (int) Math.min(b.length, todo);
            len = this.read(b, 0, len);
            if (len == EOF) {
                break;
            }
            todo -= len;
        }

        return n - todo;
    }

    // b is defined in FilterInputStream, a third party class. (If the InputStream object passed into FilterInputStream has a do-nothing close, then b is a do-nothing close)
}

public class Base16InputStreamTest {
    /**
     * Tests skipping as a noop
     *
     * @throws IOException for some failure scenarios.
     */
    @Test
    public void testSkipNone() throws IOException {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B16));
        try (final Base16InputStream b16Stream = new Base16InputStream(ins)) {
            final byte[] actualBytes = new byte[6];
            assertEquals(0, b16Stream.skip(0)); // call to a
            b16Stream.read(actualBytes, 0, actualBytes.length);
            assertArrayEquals(actualBytes, new byte[] {(byte)202, (byte)254, (byte)186, (byte)190, (byte)255, (byte)255});
            // End of stream reached
            assertEquals(-1, b16Stream.read());
        } // call to b? via try with resources
    }
}
