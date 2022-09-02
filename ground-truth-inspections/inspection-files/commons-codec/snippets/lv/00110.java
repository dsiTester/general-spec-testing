// Base32OutputStream extends BaseNCodecOutputStream, and it does not define a or b
public class BaseNCodecOutputStream {
    /**
     * Writes {@code len} bytes from the specified {@code b} array starting at {@code offset} to this
     * output stream.
     *
     * @param array
     *            source byte array
     * @param offset
     *            where to start reading the bytes
     * @param len
     *            maximum number of bytes to write
     *
     * @throws IOException
     *             if an I/O error occurs.
     * @throws NullPointerException
     *             if the byte array parameter is null
     * @throws IndexOutOfBoundsException
     *             if offset, len or buffer size are invalid
     */
    @Override
    public void write(final byte array[], final int offset, final int len) throws IOException { // definition of a
        Objects.requireNonNull(array, "array");
        if (offset < 0 || len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (offset > array.length || offset + len > array.length) {
            throw new IndexOutOfBoundsException();
        }
        if (len > 0) {
            if (doEncode) {
                baseNCodec.encode(array, offset, len, context);
            } else {
                baseNCodec.decode(array, offset, len, context);
            }
            flush(false);
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the stream.
     *
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException { // definition of b
        flush(true);
    }

    @Override
    public void close() throws IOException { // called from test
        eof();
        flush(); // call to b
        out.close();
    }
}

public class Base32OutputStreamTest {
    @Test
    public void testWriteToNullCoverage() throws Exception {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final Base32OutputStream out = new Base32OutputStream(bout)) {
            out.write(null, 0, 0); // call to a
            fail("Expcted Base32OutputStream.write(null) to throw a NullPointerException");
        } catch (final NullPointerException e) { // implicitly calls b via try-with-resources
            // Expected
        }
    }
}
