public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    @Override
    protected String getEncoding() { // used implementation of a
        return "Q";
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
    public Object decode(final Object obj) throws DecoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return decode((String) obj); // calls a and b
        }
        throw new DecoderException("Objects of type " +
              obj.getClass().getName() +
              " cannot be decoded using Q codec");
    }

    @Override
    public String decode(final String str) throws DecoderException { // called from above
        if (str == null) {
            return null;
        }
        try {
            return decodeText(str); // calls a and b
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }
}

abstract class RFC1522Codec {

    /**
     * Returns the codec name (referred to as encoding in the RFC 1522).
     *
     * @return name of the codec
     */
    protected abstract String getEncoding(); // a

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
            throws DecoderException, UnsupportedEncodingException {
        ...
        final String encoding = text.substring(from, to); // replacement value
        if (!getEncoding().equalsIgnoreCase(encoding)) { // call to a
            throw new DecoderException("This codec cannot decode " + encoding + " encoded content");
        }
        from = to + 1;
        to = text.indexOf(SEP, from);
        byte[] data = StringUtils.getBytesUsAscii(text.substring(from, to));
        data = doDecoding(data); // call to b
        return new String(data, charset);
    }

}

public class QCodecTest {
    @Test
    public void testDecodeObjects() throws Exception {
        final QCodec qcodec = new QCodec();
        final String decoded = "=?UTF-8?Q?1+1 =3D 2?=";
        final String plain = (String) qcodec.decode((Object) decoded);
        assertEquals("Basic Q decoding test",
            "1+1 = 2", plain);

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
