public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    /**
     * Gets the default Charset name used for string decoding and encoding.
     *
     * @return the default Charset name
     * @since 1.7
     */
    public Charset getCharset() { // definition of a
        return this.charset;
    }

    @Override
    public Object encode(final Object value) throws EncoderException {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return encode((String) value); // calls a
        }
        throw new EncoderException("Objects of type " +
              value.getClass().getName() +
              " cannot be encoded using BCodec");
    }

    @Override
    public String encode(final String strSource) throws EncoderException {
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset()); // call to a and b
    }

    /**
     * Encodes a string into its Base64 form using the specified Charset. Unsafe characters are escaped.
     *
     * @param strSource
     *            string to convert to Base64 form
     * @param sourceCharset
     *            the Charset for {@code value}
     * @return Base64 string
     * @throws EncoderException
     *             thrown if a failure condition is encountered during the encoding process.
     * @since 1.7
     */
    public String encode(final String strSource, final Charset sourceCharset) throws EncoderException { // definition of b
        if (strSource == null) {
            return null;
        }
        return encodeText(strSource, sourceCharset);
    }

}

public class BCodecTest {
    @Test
    public void testEncodeObjects() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "what not";
        final String encoded = (String) bcodec.encode((Object) plain); // calls a and b?

        assertEquals("Basic B encoding test", "=?UTF-8?B?d2hhdCBub3Q=?=", encoded); // assertion fails here
        ...
    }
}
