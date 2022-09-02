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
    public String encode(final String strSource) throws EncoderException { // called from test
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset()); // call to a
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

abstract class RFC1522Codec {
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

public class BCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "Hello there";
        final String encoded = bcodec.encode(plain); // calls a
        assertEquals("Basic B encoding test", "=?UTF-8?B?SGVsbG8gdGhlcmU=?=", encoded);
        assertEquals("Basic B decoding test", plain, bcodec.decode(encoded)); // calls b
    }
}
