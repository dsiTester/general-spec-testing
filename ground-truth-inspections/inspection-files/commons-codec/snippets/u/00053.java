// note that Base16InpustStream does not define a, and extends BaseNCodecInputStream
public class BaseNCodecInputStream extends FilterInputStream {
    /**
     * Reads one {@code byte} from this input stream.
     *
     * @return the byte as an integer in the range 0 to 255. Returns -1 if EOF has been reached.
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public int read() throws IOException { // definition of a
        int r = read(singleByte, 0, 1); // call to b
        while (r == 0) {
            r = read(singleByte, 0, 1);
        }
        if (r > 0) {
            final byte b = singleByte[0];
            return b < 0 ? 256 + b : b;
        }
        return EOF;
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
    public int read(final byte array[], final int offset, final int len) throws IOException { // definition of b
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

}

public class Base16InputStreamTest {
    @Test
    public void testSkipNone() throws IOException {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_B16));
        try (final Base16InputStream b16Stream = new Base16InputStream(ins)) {
            ...
            // End of stream reached
            assertEquals(-1, b16Stream.read()); // call to a
        } // implicit call to b as try with resource
    }
}
