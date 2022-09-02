public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
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
    public String decode(final String value) throws DecoderException { // definition of a
        if (value == null) {
            return null;
        }
        try {
            return this.decodeText(value); // call to b
        } catch (final UnsupportedEncodingException | IllegalArgumentException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    @Override
    protected String getEncoding() { // definition of b
        return "B";
    }

    @Override
    public Object decode(final Object value) throws DecoderException { // called from test
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return decode((String) value); // call to a
        }
        throw new DecoderException("Objects of type " +
              value.getClass().getName() +
              " cannot be decoded using BCodec");
    }

}

abstract class RFC1522Codec {

    /**
     * Returns the codec name (referred to as encoding in the RFC 1522).
     *
     * @return name of the codec
     */
    protected abstract String getEncoding(); // b

    protected String decodeText(final String text)
            throws DecoderException, UnsupportedEncodingException { // called from a
        ...
        final String encoding = text.substring(from, to);
        if (!getEncoding().equalsIgnoreCase(encoding)) { // call to b
            throw new DecoderException("This codec cannot decode " + encoding + " encoded content");
        }
        ...
    }
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
