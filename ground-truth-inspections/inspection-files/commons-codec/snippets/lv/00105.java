public class BaseNCodecOutputStream extends FilterOutputStream {
    // a is defined in FilterOutputStream, a third party class

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
    public void close() throws IOException { // definition of b; not defined in Base32OutputStream
        eof();
        flush();
        out.close();
    }

}

public class Base32InputStreamTest {
    /**
     * Tests the problem reported in CODEC-130. Missing / wrong implementation of skip.
     */
    @Test
    public void testCodec130() throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (final Base32OutputStream base32os = new Base32OutputStream(bos)) {
            base32os.write(StringUtils.getBytesUtf8(STRING_FIXTURE)); // call to a
        } // implicit call to b via try-with-resources

        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        final Base32InputStream ins = new Base32InputStream(bis);

        // we skip the first character read from the reader
        ins.skip(1);
        final byte[] decodedBytes = BaseNTestData.streamToBytes(ins, new byte[64]);
        final String str = StringUtils.newStringUtf8(decodedBytes);

        assertEquals(STRING_FIXTURE.substring(1), str); // assertion fails here
    }

}
