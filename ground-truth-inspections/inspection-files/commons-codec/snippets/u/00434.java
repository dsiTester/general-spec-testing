public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    /**
     * Encodes a string into its URL safe form using the default string charset. Unsafe characters are escaped.
     *
     * @param str
     *            string to convert to a URL safe form
     * @return URL safe string
     * @throws EncoderException
     *             Thrown if URL encoding is unsuccessful
     *
     * @see #getDefaultCharset()
     */
    @Override
    public String encode(final String str) throws EncoderException { // definition of a
        if (str == null) {
            return null;
        }
        try {
            return encode(str, getDefaultCharset()); // calls b
        } catch (final UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }

    public String encode(final String str, final String charsetName) throws UnsupportedEncodingException { // called from a
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
        final String encoded = urlCodec.encode(plain); // call to a, calls b
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);
        assertEquals("Basic URL decoding test",
            plain, urlCodec.decode(encoded));
        this.validateState(urlCodec);
    }

}
