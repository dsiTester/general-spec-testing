public class BaseNCodecOutputStream {
    /**
     * Writes EOF.
     *
     * @throws IOException
     *             if an I/O error occurs.
     * @since 1.11
     */
    public void eof() throws IOException { // definition of a
        // Notify encoder of EOF (-1).
        if (doEncode) {
            baseNCodec.encode(singleByte, 0, EOF, context);
        } else {
            baseNCodec.decode(singleByte, 0, EOF, context);
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
    public void close() throws IOException { // called from tests
        eof(); // call to a
        flush(); // call to b
        out.close();
    }
}

public class Base32OutputStreamTest {

    @Test
    public void testBase32OutputStreamByChunk() throws Exception { // validated test
        // Hello World test.
        byte[] encoded = StringUtils.getBytesUtf8(Base32TestData.BASE32_FIXTURE);
        byte[] decoded = StringUtils.getBytesUtf8(Base32TestData.STRING_FIXTURE);
        testByChunk(encoded, decoded, BaseNCodec.MIME_CHUNK_SIZE, CR_LF);

//        // Single Byte test.
//        encoded = StringUtils.getBytesUtf8("AA==\r\n");
//        decoded = new byte[]{(byte) 0};
//        testByChunk(encoded, decoded, Base32.MIME_CHUNK_SIZE, CRLF);


//        // Single Line test.
//        String singleLine = Base32TestData.ENCODED_64_CHARS_PER_LINE.replaceAll("\n", "");
//        encoded = StringUtils.getBytesUtf8(singleLine);
//        decoded = Base32TestData.DECODED;
//        testByChunk(encoded, decoded, 0, LF);

        // test random data of sizes 0 thru 150
        final BaseNCodec codec = new Base32();
        for (int i = 0; i <= 150; i++) {
            final byte[][] randomData = BaseNTestData.randomData(codec, i);
            encoded = randomData[1];
            decoded = randomData[0];
            testByChunk(encoded, decoded, 0, LF);
        }
    }

    private void testByChunk(final byte[] encoded, final byte[] decoded, final int chunkSize, final byte[] separator) throws Exception { // called by validating test

        // Start with encode.
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        OutputStream out = new Base32OutputStream(byteOut, true, chunkSize, separator);
        out.write(decoded);
        out.close();            // calls a and b
        byte[] output = byteOut.toByteArray();
        assertArrayEquals("Streaming chunked Base32 encode", encoded, output); // assertion fails here

        // Now let's try decode.
        byteOut = new ByteArrayOutputStream();
        out = new Base32OutputStream(byteOut, false);
        out.write(encoded);
        out.close();            // calls a and b
        output = byteOut.toByteArray();
        assertArrayEquals("Streaming chunked Base32 decode", decoded, output);

        // I always wanted to do this! (wrap encoder with decoder etc etc).
        byteOut = new ByteArrayOutputStream();
        out = byteOut;
        for (int i = 0; i < 10; i++) {
            out = new Base32OutputStream(out, false);
            out = new Base32OutputStream(out, true, chunkSize, separator);
        }
        out.write(decoded);
        out.close();            // calls a and b
        output = byteOut.toByteArray();

        assertArrayEquals("Streaming chunked Base32 wrap-wrap-wrap!", decoded, output);
    }

    @Test
    public void testWriteOutOfBounds() throws Exception { // invalidated test
        final byte[] buf = new byte[1024];
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final Base32OutputStream out = new Base32OutputStream(bout)) {

            try {
                out.write(buf, -1, 1);
                fail("Expected Base32OutputStream.write(buf, -1, 1) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, 1, -1);
                fail("Expected Base32OutputStream.write(buf, 1, -1) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, buf.length + 1, 0);
                fail("Expected Base32OutputStream.write(buf, buf.length + 1, 0) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, buf.length - 1, 2);
                fail("Expected Base32OutputStream.write(buf, buf.length - 1, 2) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }
        }
    } // implicitly calls a and b via try-with-resources.
}
