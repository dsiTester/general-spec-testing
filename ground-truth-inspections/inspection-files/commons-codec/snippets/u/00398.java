public class QuotedPrintableCodec implements BinaryEncoder, BinaryDecoder, StringEncoder, StringDecoder {
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
    public String decode(final String sourceStr) throws DecoderException { // definition of a
        return this.decode(sourceStr, this.getCharset()); // call to b
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
}

public class QuotedPrintableCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qpcodec.encode(plain);
        assertEquals("Basic quoted-printable encoding test",
            "=3D Hello there =3D=0D=0A", encoded);
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded)); // call to a
    }
}
