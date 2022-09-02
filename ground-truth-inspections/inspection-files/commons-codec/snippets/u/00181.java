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
        return encodeHexString(array).getBytes(this.getCharset());
    }

    /**
     * Converts a String or an array of character bytes representing hexadecimal values into an array of bytes of those
     * same values. The returned array will be half the length of the passed String or array, as it takes two characters
     * to represent any given byte. An exception is thrown if the passed char array has an odd number of elements.
     *
     * @param object A String, ByteBuffer, byte[], or an array of character bytes containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied byte array (representing characters).
     * @throws DecoderException Thrown if an odd number of characters is supplied to this function or the object is not
     *                          a String or char[]
     * @see #decodeHex(char[])
     */
    @Override
    public Object decode(final Object object) throws DecoderException { // definition of b
        if (object instanceof String) {
            return decode(((String) object).toCharArray());
        }
        if (object instanceof byte[]) {
            return decode((byte[]) object);
        }
        if (object instanceof ByteBuffer) {
            return decode((ByteBuffer) object);
        }
        try {
            return decodeHex((char[]) object);
        } catch (final ClassCastException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }
}

public class HexTest {
    @Test
    public void testRequiredCharset() throws UnsupportedEncodingException, DecoderException {
        testCustomCharset("UTF-8", "testRequiredCharset"); // calls a and b
        testCustomCharset("UTF-16", "testRequiredCharset"); // calls a and b
        testCustomCharset("UTF-16BE", "testRequiredCharset"); // calls a and b
        testCustomCharset("UTF-16LE", "testRequiredCharset"); // calls a and b
        testCustomCharset("US-ASCII", "testRequiredCharset"); // calls a and b
        testCustomCharset("ISO8859_1", "testRequiredCharset"); // calls a and b
    }

    private void testCustomCharset(final String name, final String parent) throws UnsupportedEncodingException,
            DecoderException {
        if (charsetSanityCheck(name) == false) {
            return;
        }
        log(parent + "=" + name);
        final Hex customCodec = new Hex(name);
        // source data
        final String sourceString = "Hello World";
        final byte[] sourceBytes = sourceString.getBytes(name);
        // test 1
        // encode source to hex string to bytes with charset
        final byte[] actualEncodedBytes = customCodec.encode(sourceBytes); // call to a
        // encode source to hex string...
        String expectedHexString = Hex.encodeHexString(sourceBytes);
        // ... and get the bytes in the expected charset
        final byte[] expectedHexStringBytes = expectedHexString.getBytes(name);
        Assert.assertTrue(Arrays.equals(expectedHexStringBytes, actualEncodedBytes)); // assertion fails here
        // test 2
        String actualStringFromBytes = new String(actualEncodedBytes, name);
        assertEquals(name + ", expectedHexString=" + expectedHexString + ", actualStringFromBytes=" +
                actualStringFromBytes, expectedHexString, actualStringFromBytes);
        // second test:
        final Hex utf8Codec = new Hex();
        expectedHexString = "48656c6c6f20576f726c64";
        final byte[] decodedUtf8Bytes = (byte[]) utf8Codec.decode(expectedHexString);
        actualStringFromBytes = new String(decodedUtf8Bytes, utf8Codec.getCharset());
        // sanity check:
        assertEquals(name, sourceString, actualStringFromBytes);
        // actual check:
        final byte[] decodedCustomBytes = customCodec.decode(actualEncodedBytes); // call to b
        actualStringFromBytes = new String(decodedCustomBytes, name);
        assertEquals(name, sourceString, actualStringFromBytes);
    }

}
