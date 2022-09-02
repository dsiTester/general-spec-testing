public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {

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
        return StringUtils.newStringUsAscii(encode(str.getBytes(charsetName)));
    }

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
    public String decode(final String str) throws DecoderException { // definition of b
        if (str == null) {
            return null;
        }
        try {
            return decode(str, getDefaultCharset());
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

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

}

public class URLCodecTest {
    @Test
    public void testSafeCharEncodeDecode() throws Exception { // invalidated test
        final URLCodec urlCodec = new URLCodec();
        final String plain = "abc123_-.*";             // replacement value
        final String encoded = urlCodec.encode(plain); // calls a
        assertEquals("Safe chars URL encoding test",
            plain, encoded);
        assertEquals("Safe chars URL decoding test",
            plain, urlCodec.decode(encoded)); // call to b
        this.validateState(urlCodec);
    }

    @Test
    public void testBasicEncodeDecode() throws Exception { // unknown test
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!"; // replacement value
        final String encoded = urlCodec.encode(plain); // calls a
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded); // assertion failed here
        assertEquals("Basic URL decoding test",
            plain, urlCodec.decode(encoded)); // call to b
        this.validateState(urlCodec);
    }

}
