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
     * Encodes a string into its quoted-printable form using the specified Charset. Unsafe characters are escaped.
     * <p>
     * Depending on the selection of the {@code strict} parameter, this function either implements the full ruleset
     * or only a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data and unformatted text.
     *
     * @param sourceStr
     *            string to convert to quoted-printable form
     * @param sourceCharset
     *            the Charset for sourceStr
     * @return quoted-printable string
     * @since 1.7
     */
    public String encode(final String sourceStr, final Charset sourceCharset) { // definition of b
        if (sourceStr == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(this.encode(sourceStr.getBytes(sourceCharset))); // NullPointerException here
    }

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from test
        ...
        if (obj instanceof String) {
            return encode((String) obj); // calls a and b
        }
        throw new EncoderException("Objects of type " +
              obj.getClass().getName() +
              " cannot be quoted-printable encoded");
    }

    @Override
    public String encode(final String sourceStr) throws EncoderException {
        return this.encode(sourceStr, getCharset()); // call to a; call to b
    }

}

public class QuotedPrintableCodecTest {
    @Test
    public void testEncodeObjects() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "1+1 = 2";
        String encoded = (String) qpcodec.encode((Object) plain); // calls a and b
        assertEquals("Basic quoted-printable encoding test",
            "1+1 =3D 2", encoded);

        final byte[] plainBA = plain.getBytes(StandardCharsets.UTF_8);
        final byte[] encodedBA = (byte[]) qpcodec.encode((Object) plainBA);
        encoded = new String(encodedBA);
        assertEquals("Basic quoted-printable encoding test",
            "1+1 =3D 2", encoded);

        final Object result = qpcodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            qpcodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            // Exception expected, test segment passes.
        }
    }
}
