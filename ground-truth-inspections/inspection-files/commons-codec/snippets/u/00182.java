public class Hex {
    /**
     * Converts an array of bytes into an array of bytes for the characters representing the hexadecimal values of each
     * byte in order. The returned array will be double the length of the passed array, as it takes two characters to
     * represent any given byte.
     * <p>
     * The conversion from hexadecimal characters to the returned bytes is performed with the charset named by
     * {@link #getCharset()}.
     * </p>
     *
     * @param array a byte[] to convert to hex characters
     * @return A byte[] containing the bytes of the lower-case hexadecimal characters
     * @since 1.7 No longer throws IllegalStateException if the charsetName is invalid.
     * @see #encodeHex(byte[])
     */
    @Override
    public byte[] encode(final byte[] array) { // definition of a
        return encodeHexString(array).getBytes(this.getCharset()); // call to b
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order. The returned
     * String will be double the length of the passed array, as it takes two characters to represent any given byte.
     *
     * @param data a byte[] to convert to hex characters
     * @return A String containing lower-case hexadecimal characters
     * @since 1.4
     */
    public static String encodeHexString(final byte[] data) { // definition of b
        return new String(encodeHex(data));
    }

}

public class HexTest {
    @Test
    public void testEncodeHexByteArrayEmpty() {
        assertTrue(Arrays.equals(new char[0], Hex.encodeHex(new byte[0])));
        assertTrue(Arrays.equals(new byte[0], new Hex().encode(new byte[0]))); // call to a
    }

}
