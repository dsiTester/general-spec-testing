public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

    @Override
    protected byte[] doEncoding(final byte[] bytes) { // used implementation of a
        if (bytes == null) {
            return null;
        }
        final byte[] data = QuotedPrintableCodec.encodeQuotedPrintable(PRINTABLE_CHARS, bytes);
        if (this.encodeBlanks) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == SPACE) {
                    data[i] = UNDERSCORE;
                }
            }
        }
        return data;
    }

    @Override
    protected byte[] doDecoding(final byte[] bytes) throws DecoderException { // used implementation of b
        if (bytes == null) {
            return null;
        }
        boolean hasUnderscores = false;
        for (final byte b : bytes) {
            if (b == UNDERSCORE) {
                hasUnderscores = true;
                break;
            }
        }
        if (hasUnderscores) {
            final byte[] tmp = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                final byte b = bytes[i];
                if (b != UNDERSCORE) {
                    tmp[i] = b;
                } else {
                    tmp[i] = SPACE;
                }
            }
            return QuotedPrintableCodec.decodeQuotedPrintable(tmp);
        }
        return QuotedPrintableCodec.decodeQuotedPrintable(bytes);
    }

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
        return encodeText(sourceStr, sourceCharset); // calls a
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

abstract class RFC1522Codec {
    /**
     * Encodes an array of bytes using the defined encoding scheme.
     *
     * @param bytes
     *            Data to be encoded
     * @return A byte array containing the encoded data
     * @throws EncoderException
     *             thrown if the Encoder encounters a failure condition during the encoding process.
     */
    protected abstract byte[] doEncoding(byte[] bytes) throws EncoderException; // a

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

    protected String encodeText(final String text, final Charset charset) throws EncoderException {
        if (text == null) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(PREFIX);
        buffer.append(charset);
        buffer.append(SEP);
        buffer.append(this.getEncoding());
        buffer.append(SEP);
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // call to a
        buffer.append(POSTFIX);
        return buffer.toString();
    }

    protected String decodeText(final String text)
            throws DecoderException, UnsupportedEncodingException { // called from QCodec.decode()
        ...
        byte[] data = StringUtils.getBytesUsAscii(text.substring(from, to));
        data = doDecoding(data); // call to b
        return new String(data, charset);
    }
}

public class QCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qcodec.encode(plain); // calls a
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=", encoded); // assertion fails here
        assertEquals("Basic Q decoding test",
            plain, qcodec.decode(encoded)); // call to b
    }
}
