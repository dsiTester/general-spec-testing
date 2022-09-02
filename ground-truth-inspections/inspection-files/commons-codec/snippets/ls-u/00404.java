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
        return this.encode(sourceStr, getCharset());
    }

    /**
     * Decodes an array of quoted-printable characters into an array of original bytes. Escaped characters are converted
     * back to their original representation.
     * <p>
     * This function fully implements the quoted-printable encoding specification (rule #1 through rule #5) as
     * defined in RFC 1521.
     *
     * @param bytes
     *            array of quoted-printable characters
     * @return array of original bytes
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful
     */
    @Override
    public byte[] decode(final byte[] bytes) throws DecoderException { // definition of b
        return decodeQuotedPrintable(bytes);
    }

    @Override
    public String decode(final String sourceStr) throws DecoderException { // called from test
        return this.decode(sourceStr, this.getCharset()); // calls b
    }

    public String decode(final String sourceStr, final Charset sourceCharset) throws DecoderException {
        if (sourceStr == null) {
            return null;
        }
        return new String(this.decode(StringUtils.getBytesUsAscii(sourceStr)), sourceCharset); // call to b
    }

}

public class QuotedPrintableCodecTest {
    @Test
    public void testSafeCharEncodeDecode() throws Exception { // invalidated test
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "abc123_-.*~!@#$%^&()+{}\"\\;:`,/[]";
        final String encoded = qpcodec.encode(plain); // call to a
        assertEquals("Safe chars quoted-printable encoding test",
            plain, encoded);
        assertEquals("Safe chars quoted-printable decoding test",
            plain, qpcodec.decode(encoded)); // calls b
    }

    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qpcodec.encode(plain); // call to a
        assertEquals("Basic quoted-printable encoding test",
            "=3D Hello there =3D=0D=0A", encoded); // this assertion failed
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded)); // calls b
    }
}