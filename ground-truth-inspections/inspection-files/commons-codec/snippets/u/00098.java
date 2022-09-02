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

    @Override
    public int read() throws IOException { // definition of b?
        int r = read(singleByte, 0, 1);
        while (r == 0) {
            r = read(singleByte, 0, 1);
        }
        if (r > 0) {
            final byte b = singleByte[0];
            return b < 0 ? 256 + b : b;
        }
        return EOF;
    }
}

public class Base32InputStreamTest {
    @Test
    public void testSkipNone() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
        final byte[] actualBytes = new byte[6];
        assertEquals(0, b32stream.skip(0)); // call to a?
        b32stream.read(actualBytes, 0, actualBytes.length);
        assertArrayEquals(actualBytes, new byte[] { 102, 111, 111, 0, 0, 0 });
        // End of stream reached
        b32stream.close();
        assertEquals(-1, b32stream.read()); // call to b?
    }
}
