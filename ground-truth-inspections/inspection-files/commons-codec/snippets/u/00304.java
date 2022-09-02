public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    @Override
    public Object encode(final Object value) throws EncoderException {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return encode((String) value); // call to a
        }
        throw new EncoderException("Objects of type " +
              value.getClass().getName() +
              " cannot be encoded using BCodec");
    }

    /**
     * Encodes a string into its Base64 form using the default Charset. Unsafe characters are escaped.
     *
     * @param strSource
     *            string to convert to Base64 form
     * @return Base64 string
     * @throws EncoderException
     *             thrown if a failure condition is encountered during the encoding process.
     */
    @Override
    public String encode(final String strSource) throws EncoderException { // definition of a
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset()); // calls b
    }

    public String encode(final String strSource, final Charset sourceCharset) throws EncoderException { // called from a
        if (strSource == null) {
            return null;
        }
        return encodeText(strSource, sourceCharset); // calls b
    }

    @Override
    protected String getEncoding() { // definition of b
        return "B";
    }
}

abstract class RFC1522Codec {
    /**
     * Returns the codec name (referred to as encoding in the RFC 1522).
     *
     * @return name of the codec
     */
    protected abstract String getEncoding(); // b

    protected String encodeText(final String text, final Charset charset) throws EncoderException { // called from BCodec.encode()
        if (text == null) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(PREFIX);
        buffer.append(charset);
        buffer.append(SEP);
        buffer.append(this.getEncoding()); // call to b
        buffer.append(SEP);
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset))));
        buffer.append(POSTFIX);
        return buffer.toString();
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
