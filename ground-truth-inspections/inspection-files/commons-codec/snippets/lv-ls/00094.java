public class BaseNCodecInputStream extends FilterInputStream {
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
    public int read(final byte array[], final int offset, final int len) throws IOException { // definition of a
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

    // b is defined in FilterInputStream, a third party class.
}

public class Base32InputStreamTest {
    @Test
    public void testSkipNone() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        try (final Base32InputStream b32stream = new Base32InputStream(ins)) {
            final byte[] actualBytes = new byte[6];
            assertEquals(0, b32stream.skip(0));
            b32stream.read(actualBytes, 0, actualBytes.length);
            assertArrayEquals(actualBytes, new byte[] { 102, 111, 111, 0, 0, 0 });
            // End of stream reached
            assertEquals(-1, b32stream.read()); // call to a; assertion fails here
        } // implicit call to b via try-with-resources
    }

    @Test
    public void testRead0() throws Exception {
        final byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        final byte[] buf = new byte[1024];
        int bytesRead = 0;
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        try (final Base32InputStream in = new Base32InputStream(bin, true, 4, new byte[] { 0, 0, 0 })) {
            bytesRead = in.read(buf, 0, 0); // call to a
            assertEquals("Base32InputStream.read(buf, 0, 0) returns 0", 0, bytesRead);
        } // implicit call to b via try-with-resources
    }
}
