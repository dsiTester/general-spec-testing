public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

    @Override
    public String encode(final String strSource) throws EncoderException { // called from test
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset()); // call to a
    }

    /**
     * Encodes a string into its Base64 form using the specified Charset. Unsafe characters are escaped.
     *
     * @param strSource
     *            string to convert to Base64 form
     * @param sourceCharset
     *            the Charset for {@code value}
     * @return Base64 string
     * @throws EncoderException
     *             thrown if a failure condition is encountered during the encoding process.
     * @since 1.7
     */
    public String encode(final String strSource, final Charset sourceCharset) throws EncoderException { // definition of a
        if (strSource == null) {
            return null;
        }
        return encodeText(strSource, sourceCharset); // calls b
    }

    @Override
    protected byte[] doEncoding(final byte[] bytes) { // used implementation of b
        if (bytes == null) {
            return null;
        }
        return Base64.encodeBase64(bytes);
    }

}

public abstract class RFC1522Codec {
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

    protected String encodeText(final String text, final Charset charset) throws EncoderException { // called from BCodec.encode()
        if (text == null) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(PREFIX);
        buffer.append(charset);
        buffer.append(SEP);
        buffer.append(this.getEncoding());
        buffer.append(SEP);
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // call to b
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
        assertEquals("Basic B decoding test", plain, bcodec.decode(encoded));
    }
}
