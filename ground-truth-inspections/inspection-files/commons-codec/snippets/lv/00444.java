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
    public String decode(final String str) throws DecoderException { // called from test
        if (str == null) {
            return null;
        }
        try {
            // TODO: replace the call to b with a different value as follows to test
            // return decode(str, StandardCharsets.UTF_16LE.name());
            return decode(str, getDefaultCharset()); // call to a; call to b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e); // exception thrown here
        }
    }

    public String decode(final String str, final String charsetName)
            throws DecoderException, UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        return new String(decode(StringUtils.getBytesUsAscii(str)), charsetName); // call to b; String () throws exception
    }

}

public class URLCodecTest {
    @Test
    public void testEncodeUrlWithNullBitSet() throws Exception {
        final URLCodec urlCodec = new URLCodec();
        final String plain = "Hello there!";
        final String encoded = new String( URLCodec.encodeUrl(null, plain.getBytes(StandardCharsets.UTF_8))); // replacement value
        assertEquals("Basic URL encoding test",
            "Hello+there%21", encoded);
        assertEquals("Basic URL decoding test",
            plain, urlCodec.decode(encoded)); // calls a and b
        this.validateState(urlCodec);
    }
}
