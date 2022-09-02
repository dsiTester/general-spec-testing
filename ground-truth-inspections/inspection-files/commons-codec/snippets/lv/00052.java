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

    // b is defined in FilterInputStream, which is a third party class.
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
