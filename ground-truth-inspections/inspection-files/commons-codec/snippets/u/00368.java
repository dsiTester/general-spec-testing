public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

    @Override
    public Object decode(final Object obj) throws DecoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return decode((String) obj); // call to a
        }
        throw new DecoderException("Objects of type " +
              obj.getClass().getName() +
              " cannot be decoded using Q codec");
    }

    /**
     * Decodes a quoted-printable string into its original form. Escaped characters are converted back to their original
     * representation.
     *
     * @param str
     *            quoted-printable string to convert into its original form
     * @return original string
     * @throws DecoderException
     *             A decoder exception is thrown if a failure condition is encountered during the decode process.
     */
    @Override
    public String decode(final String str) throws DecoderException { // definition of a
        if (str == null) {
            return null;
        }
        try {
            return decodeText(str); // calls b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    @Override
    protected String getEncoding() { // used implementation of b
        return "Q";
    }

}

abstract class RFC1522Codec {
    protected String decodeText(final String text)
            throws DecoderException, UnsupportedEncodingException { // called from a
        ...
        final String encoding = text.substring(from, to);
        if (!getEncoding().equalsIgnoreCase(encoding)) { // call to b
            throw new DecoderException("This codec cannot decode " + encoding + " encoded content");
        }
        ...
        return new String(data, charset);
    }

    /**
     * Returns the codec name (referred to as encoding in the RFC 1522).
     *
     * @return name of the codec
     */
    protected abstract String getEncoding(); // b
}

public class QCodecTest {
    @Test
    public void testDecodeObjects() throws Exception {
        final QCodec qcodec = new QCodec();
        final String decoded = "=?UTF-8?Q?1+1 =3D 2?=";
        final String plain = (String) qcodec.decode((Object) decoded);
        assertEquals("Basic Q decoding test",
            "1+1 = 2", plain);  // assertion fails here

        final Object result = qcodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            qcodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            // Exception expected, test segment passes.
        }
    }
}
