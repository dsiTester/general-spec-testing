public class Hex {
    /**
     * Converts byte buffer into an array of bytes for the characters representing the hexadecimal values of each byte
     * in order. The returned array will be double the length of the passed array, as it takes two characters to
     * represent any given byte.
     *
     * <p>The conversion from hexadecimal characters to the returned bytes is performed with the charset named by
     * {@link #getCharset()}.</p>
     *
     * <p>All bytes identified by {@link ByteBuffer#remaining()} will be used; after this method
     * the value {@link ByteBuffer#remaining() remaining()} will be zero.</p>
     *
     * @param array a byte buffer to convert to hex characters
     * @return A byte[] containing the bytes of the lower-case hexadecimal characters
     * @see #encodeHex(byte[])
     * @since 1.11
     */
    public byte[] encode(final ByteBuffer array) { // definition of a
        return encodeHexString(array).getBytes(this.getCharset()); // call to b
    }

    /**
     * Gets the charset.
     *
     * @return the charset.
     * @since 1.7
     */
    public Charset getCharset() { // definition of b
        return this.charset;
    }

}

public class HexTest {
    @Test
    public void testEncodeHexByteBufferEmpty() {
        assertTrue(Arrays.equals(new char[0], Hex.encodeHex(allocate(0))));
        assertTrue(Arrays.equals(new byte[0], new Hex().encode(allocate(0)))); // call to a; assertion fails here
    }
}
