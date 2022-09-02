public class URLCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    @Override
    public Object decode(final Object obj) throws DecoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return decode((byte[]) obj);
        }
        if (obj instanceof String) {
            return decode((String) obj); // call to a
        }
        throw new DecoderException("Objects of type " + obj.getClass().getName() + " cannot be URL decoded");
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
    public String decode(final String str) throws DecoderException { // definition of a
        if (str == null) {
            return null;
        }
        try {
            return decode(str, getDefaultCharset()); // call to b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
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
    public void testDecodeObjects() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello+there%21";
        String decoded = (String) urlCodec.decode((Object) plain); // calls a
        assertEquals("Basic URL decoding test",
            "Hello there!", decoded); // this assertion fails

        final byte[] plainBA = plain.getBytes(StandardCharsets.UTF_8);
        final byte[] decodedBA = (byte[]) urlCodec.decode((Object) plainBA);
        decoded = new String(decodedBA);
        assertEquals("Basic URL decoding test",
            "Hello there!", decoded);

        final Object result = urlCodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            urlCodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            // Exception expected, test segment passes.
        }
        this.validateState(urlCodec);
    }

}
