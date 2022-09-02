public class QuotedPrintableCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
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
    public String encode(final String sourceStr, final Charset sourceCharset) { // definition of a
        if (sourceStr == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(this.encode(sourceStr.getBytes(sourceCharset)));
    }

    @Override
    public String encode(final String sourceStr) throws EncoderException { // called from test
        return this.encode(sourceStr, getCharset()); // call to a
    }

    /**
     * Decodes a quoted-printable string into its original form using the default string Charset. Escaped characters are
     * converted back to their original representation.
     *
     * @param sourceStr
     *            quoted-printable string to convert into its original form
     * @return original string
     * @throws DecoderException
     *             Thrown if quoted-printable decoding is unsuccessful. Thrown if Charset is not supported.
     * @see #getCharset()
     */
    @Override
    public String decode(final String sourceStr) throws DecoderException { // definition of b
        return this.decode(sourceStr, this.getCharset());
    }

}

public class QuotedPrintableCodecTest {
    @Test
    public void testSafeCharEncodeDecode() throws Exception { // invalidated test
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "abc123_-.*~!@#$%^&()+{}\"\\;:`,/[]";
        final String encoded = qpcodec.encode(plain); // calls a
        assertEquals("Safe chars quoted-printable encoding test",
            plain, encoded);
        assertEquals("Safe chars quoted-printable decoding test",
            plain, qpcodec.decode(encoded)); // call to b
    }

    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qpcodec.encode(plain); // calls a
        assertEquals("Basic quoted-printable encoding test",
            "=3D Hello there =3D=0D=0A", encoded); // this assertion failed
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded)); // call to b
    }
}
