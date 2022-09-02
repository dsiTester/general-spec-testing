public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    /**
     * Decodes a URL safe string into its original form using the default string charset. Escaped characters are
     * converted back to their original representation.
     *
     * @param str
     *            URL safe string to convert into its original form
     * @return original string
     * @throws DecoderException
     *             Thrown if URL decoding is unsuccessful
     * @see #getDefaultCharset()
     */
    @Override
    public String decode(final String str) throws DecoderException { // definition of a
        if (str == null) {
            return null;
        }
        try {
            return decode(str, getDefaultCharset()); // calls b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    public String decode(final String str, final String charsetName) // called from a
            throws DecoderException, UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        return new String(decode(StringUtils.getBytesUsAscii(str)), charsetName); // call to b
    }

    /**
     * Decodes an array of URL safe 7-bit characters into an array of original bytes. Escaped characters are converted
     * back to their original representation.
     *
     * @param bytes
     *            array of URL safe characters
     * @return array of original bytes
     * @throws DecoderException
     *             Thrown if URL decoding is unsuccessful
     */
    @Override
    public byte[] decode(final byte[] bytes) throws DecoderException { // definition of b
        return decodeUrl(bytes);
    }
}

public class URLCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!";
        final String encoded = urlCodec.encode(plain); // replacement value
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);
        assertEquals("Basic URL decoding test",
            plain, urlCodec.decode(encoded)); // call to a; assertion fails here
        this.validateState(urlCodec);
    }

}
