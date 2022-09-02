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

    /**
     * Attempts to read {@code len} bytes into the specified {@code b} array starting at {@code offset}
     * from this InputStream.
     *
     * @param array
     *            destination byte array
     * @param offset
     *            where to start writing the bytes
     * @param len
     *            maximum number of bytes to read
     *
     * @return number of bytes read
     * @throws IOException
     *             if an I/O error occurs.
     * @throws NullPointerException
     *             if the byte array parameter is null
     * @throws IndexOutOfBoundsException
     *             if offset, len or buffer size are invalid
     */
    @Override
    public int read(final byte array[], final int offset, final int len) throws IOException { // definition of b?
        Objects.requireNonNull(array, "array");
        if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (offset > array.length || offset + len > array.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return 0;
        }
        int readLen = 0;
        ...
        while (readLen == 0) {
            if (!baseNCodec.hasData(context)) {
                final byte[] buf = new byte[doEncode ? 4096 : 8192];
                final int c = in.read(buf);
                if (doEncode) {
                    baseNCodec.encode(buf, 0, c, context);
                } else {
                    baseNCodec.decode(buf, 0, c, context);
                }
            }
            readLen = baseNCodec.readResults(array, offset, len, context);
        }
        return readLen;
    }

}

public class Base32InputStreamTest {
    @Test
    public void testSkipNone() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        final Base32InputStream b32stream = new Base32InputStream(ins);
            final byte[] actualBytes = new byte[6];
            assertEquals(0, b32stream.skip(0)); // call to a
            b32stream.read(actualBytes, 0, actualBytes.length); // call to b
            assertArrayEquals(actualBytes, new byte[] { 102, 111, 111, 0, 0, 0 });
            // End of stream reached
        b32stream.close();
        assertEquals(-1, b32stream.read());
    }
}
