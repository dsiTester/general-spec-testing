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
        /*
         Rationale for while-loop on (readLen == 0):
         -----
         Base32.readResults() usually returns > 0 or EOF (-1).  In the
         rare case where it returns 0, we just keep trying.

         This is essentially an undocumented contract for InputStream
         implementors that want their code to work properly with
         java.io.InputStreamReader, since the latter hates it when
         InputStream.read(byte[]) returns a zero.  Unfortunately our
         readResults() call must return 0 if a large amount of the data
         being decoded was non-base32, so this while-loop enables proper
         interop with InputStreamReader for that scenario.
         -----
         This is a fix for CODEC-101
        */
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

public class Base16InputStreamTest {
    /**
     * Tests read with null.
     *
     * @throws IOException for some failure scenarios.
     */
    @Test
    public void testReadNull() throws IOException { // validated test
        final byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        try (final Base16InputStream in = new Base16InputStream(bin, true)) {
            in.read(null, 0, 0); // call to a
            fail("Base16InputStream.read(null, 0, 0) to throw a NullPointerException");
        } catch (final NullPointerException e) { // implicit call to b here as try with resources
            // Expected
        }
    }

    @Test
    public void testRead0() throws IOException { // invalidated test
        final byte[] decoded = StringUtils.getBytesUtf8(STRING_FIXTURE);
        final byte[] buf = new byte[1024];
        int bytesRead = 0;
        final ByteArrayInputStream bin = new ByteArrayInputStream(decoded);
        try (final Base16InputStream in = new Base16InputStream(bin, true)) {
            bytesRead = in.read(buf, 0, 0); // call to a
            assertEquals("Base16InputStream.read(buf, 0, 0) returns 0", 0, bytesRead);
        } // implicit call to b here
    }


}
