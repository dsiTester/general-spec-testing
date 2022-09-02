public class QuotedPrintableCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {

    /**
     * Gets the default Charset name used for string decoding and encoding.
     *
     * @return the default Charset name
     * @since 1.7
     */
    public Charset getCharset() { // definition of a
        return this.charset;
    }

    /**
     * Decodes a quoted-printable string into its original form using the specified string Charset. Escaped characters
     * are converted back to their original representation.
     *
     * @param sourceStr
     *            quoted-printable string to convert into its original form
     * @param sourceCharset
     *            the original string Charset
     * @return original string
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful
     * @since 1.7
     */
    public String decode(final String sourceStr, final Charset sourceCharset) throws DecoderException { // definition of b
        if (sourceStr == null) {
            return null;
        }
        return new String(this.decode(StringUtils.getBytesUsAscii(sourceStr)), sourceCharset); // NullPointerException here
    }

    @Override
    public Object decode(final Object obj) throws DecoderException { // called from test
        ...
        if (obj instanceof String) {
            return decode((String) obj); // calls a and b
        }
        throw new DecoderException("Objects of type " +
              obj.getClass().getName() +
              " cannot be quoted-printable decoded");
    }

    @Override
    public String decode(final String sourceStr) throws DecoderException {
        return this.decode(sourceStr, this.getCharset()); // call to a; call to b
    }
}

public class QuotedPrintableCodecTest {
    @Test
    public void testDecodeObjects() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "1+1 =3D 2";
        String decoded = (String) qpcodec.decode((Object) plain); // calls a
        assertEquals("Basic quoted-printable decoding test",
            "1+1 = 2", decoded);

        final byte[] plainBA = plain.getBytes(StandardCharsets.UTF_8);
        final byte[] decodedBA = (byte[]) qpcodec.decode((Object) plainBA);
        decoded = new String(decodedBA);
        assertEquals("Basic quoted-printable decoding test",
            "1+1 = 2", decoded);

        final Object result = qpcodec.decode((Object) null);
        assertEquals( "Decoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            qpcodec.decode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            // Exception expected, test segment passes.
        }
    }

}
