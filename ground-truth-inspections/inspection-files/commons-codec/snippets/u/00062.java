// a and b are not defined in Base16OutputStream, and Base16OutputStream extends BaseNCodecOutputStream
public class BaseNCodecOutputStream extends FilterOutputStream {
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
        flush(); // call to b
        out.close();
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

}

public class Base16OutputStreamTest {
    /**
     * Tests Base16OutputStream.write(null).
     *
     * @throws IOException for some failure scenarios.
     */
    @Test
    public void testWriteToNullCoverage() throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final Base16OutputStream out = new Base16OutputStream(bout)) {
            out.write(null, 0, 0);
            fail("Expcted Base16OutputStream.write(null) to throw a NullPointerException");
        } catch (final NullPointerException e) { // implicit call to a via try-with-resources
            // Expected
        }
    }
}
