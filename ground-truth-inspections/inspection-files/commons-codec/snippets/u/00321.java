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
    protected byte[] doEncoding(final byte[] bytes) { // used implementation of b
        if (bytes == null) {
            return null;
        }
        return Base64.encodeBase64(bytes);
    }

    @Override
    public String encode(final String strSource) throws EncoderException { // called from test
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset()); // call to a; encode() calls RFC1522Codec.encodeText, which throws exception
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
    protected abstract byte[] doEncoding(byte[] bytes) throws EncoderException; // b

    protected String encodeText(final String text, final Charset charset) throws EncoderException {
        ...
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // call to b; also NullPointerException here
        buffer.append(POSTFIX);
        return buffer.toString();
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
