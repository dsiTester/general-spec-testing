public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[])obj);
        }
        if (obj instanceof String) {
            return encode((String)obj); // call to a
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be URL encoded");
    }

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
            return encode(str, getDefaultCharset()); // call to b
        } catch (final UnsupportedEncodingException e) {
            throw new EncoderException(e.getMessage(), e);
        }
    }

    /**
     * The default charset used for string decoding and encoding.
     *
     * @return the default string charset.
     */
    public String getDefaultCharset() { // definition of b
        return this.charset;
    }
}

public class URLCodecTest {
    @Test
    public void testEncodeObjects() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!";
        String encoded = (String) urlCodec.encode((Object) plain); // calls a and b
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded); // assertion failed here

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
