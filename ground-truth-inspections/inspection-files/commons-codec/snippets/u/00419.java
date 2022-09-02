public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

    @Override
    protected byte[] doEncoding(final byte[] bytes) { // used implementation of a
        if (bytes == null) {
            return null;
        }
        return Base64.encodeBase64(bytes);
    }

    /**
     * Decodes a Base64 string into its original form. Escaped characters are converted back to their original
     * representation.
     *
     * @param value
     *            Base64 string to convert into its original form
     * @return original string
     * @throws DecoderException
     *             A decoder exception is thrown if a failure condition is encountered during the decode process.
     */
    @Override
    public String decode(final String value) throws DecoderException { // definition of b
        if (value == null) {
            return null;
        }
        try {
            return this.decodeText(value);
        } catch (final UnsupportedEncodingException | IllegalArgumentException e) {
            throw new DecoderException(e.getMessage(), e);
        }
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
        return encodeText(strSource, sourceCharset); // calls a
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

    protected String encodeText(final String text, final Charset charset) throws EncoderException {
        ...
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // call to a
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
        assertEquals("Basic B encoding test", "=?UTF-8?B?SGVsbG8gdGhlcmU=?=", encoded); // assertion fails here
        assertEquals("Basic B decoding test", plain, bcodec.decode(encoded)); // call to b
    }
}
