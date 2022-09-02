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
        buffer.append(this.getEncoding());
        buffer.append(SEP);
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset))));
        buffer.append(POSTFIX);
        return buffer.toString();
    }

    /**
     * Applies an RFC 1522 compliant decoding scheme to the given string of text.
     * <p>
     * This method processes the "encoded-word" header common to all the RFC 1522 codecs and then invokes
     * {@link #doDecoding(byte[])}  method of a concrete class to perform the specific decoding.
     *
     * @param text
     *            a string to decode
     * @return A new decoded String or {@code null} if the input is {@code null}.
     * @throws DecoderException
     *             thrown if there is an error condition during the decoding process.
     * @throws UnsupportedEncodingException
     *             thrown if charset specified in the "encoded-word" header is not supported
     */
    protected String decodeText(final String text)
            throws DecoderException, UnsupportedEncodingException { // definition of b
        if (text == null) {
            return null;
        }
        if (!text.startsWith(PREFIX) || !text.endsWith(POSTFIX)) {
            throw new DecoderException("RFC 1522 violation: malformed encoded content");
        }
        final int terminator = text.length() - 2;
        int from = 2;
        int to = text.indexOf(SEP, from);
        if (to == terminator) {
            throw new DecoderException("RFC 1522 violation: charset token not found");
        }
        final String charset = text.substring(from, to);
        if (charset.equals("")) {
            throw new DecoderException("RFC 1522 violation: charset not specified");
        }
        from = to + 1;
        to = text.indexOf(SEP, from);
        if (to == terminator) {
            throw new DecoderException("RFC 1522 violation: encoding token not found");
        }
        final String encoding = text.substring(from, to);
        if (!getEncoding().equalsIgnoreCase(encoding)) {
            throw new DecoderException("This codec cannot decode " + encoding + " encoded content");
        }
        from = to + 1;
        to = text.indexOf(SEP, from);
        byte[] data = StringUtils.getBytesUsAscii(text.substring(from, to));
        data = doDecoding(data);
        return new String(data, charset);
    }

}

public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

    @Override
    public String encode(final String sourceStr) throws EncoderException { // called from test
        if (sourceStr == null) {
            return null;
        }
        return encode(sourceStr, getCharset()); // calls a
    }

    public String encode(final String sourceStr, final Charset sourceCharset) throws EncoderException {
        if (sourceStr == null) {
            return null;
        }
        return encodeText(sourceStr, sourceCharset); // call to a
    }

    @Override
    public String decode(final String str) throws DecoderException { // called from test
        if (str == null) {
            return null;
        }
        try {
            return decodeText(str); // call to b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

}

public class QCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "= Hello there =\r\n";  // replacement value of a
        final String encoded = qcodec.encode(plain); // calls a
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=", encoded); // this assertion failed
        assertEquals("Basic Q decoding test",
            plain, qcodec.decode(encoded)); // calls b
    }
}
