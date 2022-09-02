public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    /**
     * The default charset used for string decoding and encoding.
     *
     * @return the default string charset.
     */
    public String getDefaultCharset() { // definition of a
        return this.charset;
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
    public String encode(final String str, final String charsetName) throws UnsupportedEncodingException { // definition of b
        if (str == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(encode(str.getBytes(charsetName))); // throws exception
    }

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from test
        ...
        if (obj instanceof String) {
            return encode((String)obj); // calls b
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be URL encoded");
    }

    @Override
    public String encode(final String str) throws EncoderException {
        if (str == null) {
            return null;
        }
        try {
            // TODO: replace the call to a to a different value as follows; the test would fail.
            // return encode(str, StandardCharsets.UTF_16LE.name());
            return encode(str, getDefaultCharset()); // call to a; call to b
        } catch (final UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e); // throws exception
        }
    }
}

public class URLCodecTest {
    @Test
    public void testEncodeObjects() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!"; // replacement value
        String encoded = (String) urlCodec.encode((Object) plain); // calls a and b
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);

        final byte[] plainBA = plain.getBytes(StandardCharsets.UTF_8);
        final byte[] encodedBA = (byte[]) urlCodec.encode((Object) plainBA);
        encoded = new String(encodedBA);
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);

        final Object result = urlCodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            urlCodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            // Exception expected, test segment passes.
        }
        this.validateState(urlCodec);
    }


}
