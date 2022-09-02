public class RFC1522Codec {
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
     * Decodes an array of bytes using the defined encoding scheme.
     *
     * @param bytes
     *            Data to be decoded
     * @return a byte array that contains decoded data
     * @throws DecoderException
     *             A decoder exception is thrown if a Decoder encounters a failure condition during the decode process.
     */
    protected abstract byte[] doDecoding(byte[] bytes) throws DecoderException; // b

    protected String decodeText(final String text)
            throws DecoderException, UnsupportedEncodingException { // called from BCodec.decode()
        ...
        data = doDecoding(data); // call to b
        return new String(data, charset);
    }

}

public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

    @Override
    protected byte[] doDecoding(final byte[] bytes) { // used implementation of b
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes);
    }

    @Override
    public String encode(final String strSource) throws EncoderException { // called from test
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset()); // calls a
    }

    public String encode(final String strSource, final Charset sourceCharset) throws EncoderException {
        if (strSource == null) {
            return null;
        }
        return encodeText(strSource, sourceCharset); // call to a
    }

    @Override
    public String decode(final String value) throws DecoderException { // called from test
        if (value == null) {
            return null;
        }
        try {
            return this.decodeText(value); // call to b
        } catch (final UnsupportedEncodingException | IllegalArgumentException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

}

public class BCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "Hello there";
        final String encoded = bcodec.encode(plain); // calls a
        assertEquals("Basic B encoding test", "=?UTF-8?B?SGVsbG8gdGhlcmU=?=", encoded); // assertion failed here
        assertEquals("Basic B decoding test", plain, bcodec.decode(encoded)); // calls b
    }
}
