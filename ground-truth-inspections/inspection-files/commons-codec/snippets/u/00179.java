public class Hex {
    /**
     * Converts a String or an array of bytes into an array of characters representing the hexadecimal values of each
     * byte in order. The returned array will be double the length of the passed String or array, as it takes two
     * characters to represent any given byte.
     * <p>
     * The conversion from hexadecimal characters to bytes to be encoded to performed with the charset named by
     * {@link #getCharset()}.
     * </p>
     *
     * @param object a String, ByteBuffer, or byte[] to convert to hex characters
     * @return A char[] containing lower-case hexadecimal characters
     * @throws EncoderException Thrown if the given object is not a String or byte[]
     * @see #encodeHex(byte[])
     */
    @Override
    public Object encode(final Object object) throws EncoderException { // definition of a
        final byte[] byteArray;
        if (object instanceof String) {
            byteArray = ((String) object).getBytes(this.getCharset()); // call to b
        } else if (object instanceof ByteBuffer) {
            byteArray = toByteArray((ByteBuffer) object);
        } else {
            try {
                byteArray = (byte[]) object;
            } catch (final ClassCastException e) {
                throw new EncoderException(e.getMessage(), e);
            }
        }
        return encodeHex(byteArray);
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
    public void testEncodeStringEmpty() throws EncoderException {
        assertTrue(Arrays.equals(new char[0], (char[]) new Hex().encode(""))); // call to a; fails here
    }
}
