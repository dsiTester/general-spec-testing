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
        return new String(this.decode(StringUtils.getBytesUsAscii(sourceStr)), sourceCharset);
    }

    @Override
    public String encode(final String sourceStr) throws EncoderException { // called from test
        return this.encode(sourceStr, getCharset()); // calls a
    }

    public String encode(final String sourceStr, final Charset sourceCharset) {
        if (sourceStr == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(this.encode(sourceStr.getBytes(sourceCharset))); // call to a
    }

    @Override
    public String decode(final String sourceStr) throws DecoderException { // called from test
        return this.decode(sourceStr, this.getCharset()); // call to b
    }

}

public class QuotedPrintableCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qpcodec.encode(plain); // calls a
        assertEquals("Basic quoted-printable encoding test",
            "=3D Hello there =3D=0D=0A", encoded); // this assertion failed
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded)); // calls b
    }
}
