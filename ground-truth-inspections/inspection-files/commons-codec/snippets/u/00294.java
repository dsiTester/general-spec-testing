public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    @Override
    protected byte[] doDecoding(final byte[] bytes) { // used implementation of b
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
            return this.decodeText(value); // call to a
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
            throws DecoderException, UnsupportedEncodingException { // definition of a, shortened.
        ...
        data = doDecoding(data); // call to b
        return new String(data, charset);
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

}

public class BCodecTest {
    @Test
    public void testDecodeObjects() throws Exception {
        final BCodec bcodec = new BCodec();
        final String decoded = "=?UTF-8?B?d2hhdCBub3Q=?=";
        final String plain = (String) bcodec.decode((Object) decoded); // calls a and b?
        assertequals("Basic B decoding test", "what not", plain); // assertion fails here

        final Object result = bcodec.decode((Object) null);
        assertEquals("Decoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            bcodec.decode(dObj);
            fail("Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            // Exception expected, test segment passes.
        }
    }

}
