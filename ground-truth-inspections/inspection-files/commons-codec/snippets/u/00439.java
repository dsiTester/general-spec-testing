public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    @Override
    public String encode(final String str) throws EncoderException { // called from test
        if (str == null) {
            return null;
        }
        try {
            return encode(str, getDefaultCharset()); // call to a
        } catch (final UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }

    /**
     * Encodes a string into its URL safe form using the specified string charset. Unsafe characters are escaped.
     *
     * @param str
     *            string to convert to a URL safe form
     * @param charsetName
     *            the charset for str
     * @return URL safe string
     * @throws UnsupportedEncodingException
     *             Thrown if charset is not supported
     */
    public String encode(final String str, final String charsetName) throws UnsupportedEncodingException { // definition of a
        if (str == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(encode(str.getBytes(charsetName))); // call to b
    }

    /**
     * Encodes an array of bytes into an array of URL safe 7-bit characters. Unsafe characters are escaped.
     *
     * @param bytes
     *            array of bytes to convert to URL safe characters
     * @return array of bytes containing URL safe characters
     */
    @Override
    public byte[] encode(final byte[] bytes) { // definition of b
        return encodeUrl(WWW_FORM_URL_SAFE, bytes);
    }
}

public class URLCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!"; // replacement value
        final String encoded = urlCodec.encode(plain); // calls a, calls b
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);
        assertEquals("Basic URL decoding test",
            plain, urlCodec.decode(encoded));
        this.validateState(urlCodec);
    }

}
