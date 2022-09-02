// Base32OutputStream extends BaseNCodecOutputStream, and does not implement either a or b
public class BaseNCodecOutputStream {
    /**
     * Closes this output stream and releases any system resources associated with the stream.
     * <p>
     * To write the EOF marker without closing the stream, call {@link #eof()} or use an
     * <a href="https://commons.apache.org/proper/commons-io/">Apache Commons IO</a> <a href=
     * "https://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/output/CloseShieldOutputStream.html"
     * >CloseShieldOutputStream</a>.
     * </p>
     *
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public void close() throws IOException { // definition of a
        eof();
        flush(); // calls b
        out.close();
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written out to the stream.
     *
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public void flush() throws IOException { // called from a
        flush(true); // call to b
    }
}

public class Base32OutputStreamTest {
    @Test
    public void testWriteToNullCoverage() throws Exception {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final Base32OutputStream out = new Base32OutputStream(bout)) {
            out.write(null, 0, 0);
            fail("Expcted Base32OutputStream.write(null) to throw a NullPointerException");
        } catch (final NullPointerException e) { // implicit call to a here via try-with-resources
            // Expected
        }
    }
}
