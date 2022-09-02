public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {

    /**
     * Encodes an array of bytes into an array of URL safe 7-bit characters. Unsafe characters are escaped.
     *
     * @param bytes
     *            array of bytes to convert to URL safe characters
     * @return array of bytes containing URL safe characters
     */
    @Override
    public byte[] encode(final byte[] bytes) { // definition of a
        return encodeUrl(WWW_FORM_URL_SAFE, bytes);
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

    @Override
    public String encode(final String str) throws EncoderException { // called from test
        if (str == null) {
            return null;
        }
        try {
            return encode(str, getDefaultCharset()); // calls a
        } catch (final UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }

    public String encode(final String str, final String charsetName) throws UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(encode(str.getBytes(charsetName))); // call to a
    }

    @Override
    public String decode(final String str) throws DecoderException { // called from test
        if (str == null) {
            return null;
        }
        try {
            return decode(str, getDefaultCharset()); // calls b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    public String decode(final String str, final String charsetName)
            throws DecoderException, UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        return new String(decode(StringUtils.getBytesUsAscii(str)), charsetName); // call to b
    }

}

public class URLCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
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
