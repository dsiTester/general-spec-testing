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
        int r = read(singleByte, 0, 1);
        while (r == 0) {
            r = read(singleByte, 0, 1);
        }
        if (r > 0) {
            final byte b = singleByte[0];
            return b < 0 ? 256 + b : b;
        }
        return EOF;
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

}
