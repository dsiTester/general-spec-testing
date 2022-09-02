public class QuotedPrintableCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
    /**
     * Encodes a string into its quoted-printable form using the default string Charset. Unsafe characters are escaped.
     * <p>
     * Depending on the selection of the {@code strict} parameter, this function either implements the full ruleset
     * or only a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data and unformatted text.
     *
     * @param sourceStr
     *            string to convert to quoted-printable form
     * @return quoted-printable string
     * @throws EncoderException
     *             Thrown if quoted-printable encoding is unsuccessful
     *
     * @see #getCharset()
     */
    @Override
    public String encode(final String sourceStr) throws EncoderException { // definition of a
        return this.encode(sourceStr, getCharset()); // call to b
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
        return StringUtils.newStringUsAscii(this.encode(sourceStr.getBytes(sourceCharset)));
    }

}

public class QuotedPrintableCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qpcodec.encode(plain); // call to a; calls b
        assertEquals("Basic quoted-printable encoding test",
            "=3D Hello there =3D=0D=0A", encoded); // this assertion failed
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded));
    }
}
