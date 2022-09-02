public class Hex {

    @Override
    public Object decode(final Object object) throws DecoderException { // called from test
        if (object instanceof String) {
            return decode(((String) object).toCharArray());
        }
        if (object instanceof byte[]) {
            return decode((byte[]) object);
        }
        if (object instanceof ByteBuffer) {
            return decode((ByteBuffer) object); // call to a
        }
        try {
            return decodeHex((char[]) object);
        } catch (final ClassCastException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    /**
     * Converts a buffer of character bytes representing hexadecimal values into an array of bytes of those same values.
     * The returned array will be half the length of the passed array, as it takes two characters to represent any given
     * byte. An exception is thrown if the passed char array has an odd number of elements.
     *
     * <p>All bytes identified by {@link ByteBuffer#remaining()} will be used; after this method
     * the value {@link ByteBuffer#remaining() remaining()} will be zero.</p>
     *
     * @param buffer An array of character bytes containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied byte array (representing characters).
     * @throws DecoderException Thrown if an odd number of characters is supplied to this function
     * @see #decodeHex(char[])
     * @since 1.11
     */
    @Override
    public byte[] decode(final byte[] array) throws DecoderException { // definition of a
        return decodeHex(new String(array, getCharset()).toCharArray()); // call to b
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
    public void testDecodeByteArrayObjectEmpty() throws DecoderException {
        assertTrue(Arrays.equals(new byte[0], (byte[]) new Hex().decode((Object) new byte[0]))); // call to a; assertion fails here as a was never called
    }

}
