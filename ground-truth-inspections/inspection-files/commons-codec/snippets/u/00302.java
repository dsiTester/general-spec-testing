public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    /**
     * Encodes a string into its Base64 form using the default Charset. Unsafe characters are escaped.
     *
     * @param strSource
     *            string to convert to Base64 form
     * @return Base64 string
     * @throws EncoderException
     *             thrown if a failure condition is encountered during the encoding process.
     */
    @Override
    public String encode(final String strSource) throws EncoderException { // definition of a
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset());
    }

    @Override
    protected byte[] doDecoding(final byte[] bytes) { // definition of b
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes);
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

public abstract class RFC1522Codec {
    protected String decodeText(final String text)
            throws DecoderException, UnsupportedEncodingException { // called from BCodec.decode()
        ...
        byte[] data = StringUtils.getBytesUsAscii(text.substring(from, to));
        data = doDecoding(data); // call to b
        return new String(data, charset);
    }
}

public class BCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "Hello there";
        final String encoded = bcodec.encode(plain); // call to a
        assertEquals("Basic B encoding test", "=?UTF-8?B?SGVsbG8gdGhlcmU=?=", encoded); // assertion failed here
        assertEquals("Basic B decoding test", plain, bcodec.decode(encoded)); // calls b
    }
}
