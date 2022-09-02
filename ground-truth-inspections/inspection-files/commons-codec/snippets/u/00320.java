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
        return encode(strSource, this.getCharset()); // call to a; encode() calls RFC1522Codec.encodeText, which throws exception
    }

    @Override
    public String decode(final String value) throws DecoderException { // called from test
        if (value == null) {
            return null;
        }
        try {
            return this.decodeText(value); // calls b
        } catch (final UnsupportedEncodingException | IllegalArgumentException e) {
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

    protected String encodeText(final String text, final Charset charset) throws EncoderException {
        ...
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // NullPointerException here
        buffer.append(POSTFIX);
        return buffer.toString();
    }

    protected String decodeText(final String text)
            throws DecoderException, UnsupportedEncodingException { // called from BCodec.decode()
        ...
        data = doDecoding(data); // call to b
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
