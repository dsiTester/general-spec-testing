// a and b are not defined in Base16OutputStream, and Base16OutputStream extends BaseNCodecOutputStream
public class BaseNCodecOutputStream extends FilterOutputStream {
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
     * Writes EOF.
     *
     * @throws IOException
     *             if an I/O error occurs.
     * @since 1.11
     */
    public void eof() throws IOException { // definition of b
        // Notify encoder of EOF (-1).
        if (doEncode) {
            baseNCodec.encode(singleByte, 0, EOF, context);
        } else {
            baseNCodec.decode(singleByte, 0, EOF, context);
        }
    }

    @Override
    public void close() throws IOException { // implicitly called from test
        eof(); // call to b
        flush();
        out.close();
    }
}

public class Base16OutputStreamTest {
    @Test
    public void testWriteToNullCoverage() throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final Base16OutputStream out = new Base16OutputStream(bout)) {
            out.write(null, 0, 0); // call to a
            fail("Expcted Base16OutputStream.write(null) to throw a NullPointerException"); // assertion fails here
        } catch (final NullPointerException e) { // calls b implicitly as try-with-resources
            // Expected
        }
    }
}
