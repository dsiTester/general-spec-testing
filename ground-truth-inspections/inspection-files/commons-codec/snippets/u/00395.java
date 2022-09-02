public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

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
    protected byte[] doDecoding(final byte[] bytes) throws DecoderException { // definition of b
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
        return encode(sourceStr, getCharset()); // call to a
    }

    @Override
    public String decode(final String str) throws DecoderException { // called from test
        if (str == null) {
            return null;
        }
        try {
            return decodeText(str); // calls b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

}

abstract class RFC1522Codec {

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
            throws DecoderException, UnsupportedEncodingException { // called from QCodec.decode()
        ...
        byte[] data = StringUtils.getBytesUsAscii(text.substring(from, to));
        data = doDecoding(data);
        return new String(data, charset);
    }

    protected String encodeText(final String text, final Charset charset) throws EncoderException {
        ...
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // NullPointerException thrown here
        buffer.append(POSTFIX);
        return buffer.toString();
    }

}

public class QCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qcodec.encode(plain); // calls a
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=", encoded);
        assertEquals("Basic Q decoding test",
            plain, qcodec.decode(encoded)); // calls b
    }
}
