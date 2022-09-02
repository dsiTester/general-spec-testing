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


    @Override
    public int read() throws IOException {
        int r = read(singleByte, 0, 1); // call to a
        while (r == 0) {
            r = read(singleByte, 0, 1);
        }
        ...
        return EOF;
    }

    // b is defined in FilterInputStream, a third party class
}

public class Base16InputStreamTest {
    @Test
    public void testSkipNone() throws IOException {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B16));
        try (final Base16InputStream b16Stream = new Base16InputStream(ins)) {
            final byte[] actualBytes = new byte[6];
            assertEquals(0, b16Stream.skip(0));
            b16Stream.read(actualBytes, 0, actualBytes.length);
            assertArrayEquals(actualBytes, new byte[] {(byte)202, (byte)254, (byte)186, (byte)190, (byte)255, (byte)255});
            // End of stream reached
            assertEquals(-1, b16Stream.read()); // calls a
        } // implicit call to b via try-with-resources
    }
}
