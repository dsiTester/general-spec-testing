public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
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
        return encode(strSource, this.getCharset()); // calls a
    }

    public String encode(final String strSource, final Charset sourceCharset) throws EncoderException {
        if (strSource == null) {
            return null;
        }
        return encodeText(strSource, sourceCharset); // calls a
    }

    @Override
    protected String getEncoding() { // definition of b
        return "B";
    }
}

abstract class RFC1522Codec {

    /**
     * Applies an RFC 1522 compliant encoding scheme to the given string of text with the given charset.
     * <p>
     * This method constructs the "encoded-word" header common to all the RFC 1522 codecs and then invokes
     * {@link #doEncoding(byte[])}  method of a concrete class to perform the specific encoding.
     *
     * @param text
     *            a string to encode
     * @param charset
     *            a charset to be used
     * @return RFC 1522 compliant "encoded-word"
     * @throws EncoderException
     *             thrown if there is an error condition during the Encoding process.
     * @see <a href="http://download.oracle.com/javase/7/docs/api/java/nio/charset/Charset.html">Standard charsets</a>
     */
    protected String encodeText(final String text, final Charset charset) throws EncoderException { // definition of a
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

    /**
     * Returns the codec name (referred to as encoding in the RFC 1522).
     *
     * @return name of the codec
     */
    protected abstract String getEncoding(); // b

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
