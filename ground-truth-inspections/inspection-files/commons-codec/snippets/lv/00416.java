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
        return this.decode(sourceStr, this.getCharset()); // call to a; calls b
    }

    public String decode(final String sourceStr, final Charset sourceCharset) throws DecoderException {
        if (sourceStr == null) {
            return null;
        }
        return new String(this.decode(StringUtils.getBytesUsAscii(sourceStr)), sourceCharset); // call to b; NullPointerException here
    }

}

public class QuotedPrintableCodecTest {
    @Test
    public void testEncodeUrlWithNullBitSet() throws Exception {
        final QuotedPrintableCodec qpcodec = new QuotedPrintableCodec();
        final String plain = "1+1 = 2";
        final String encoded = new String(QuotedPrintableCodec.
            encodeQuotedPrintable(null, plain.getBytes(StandardCharsets.UTF_8)));
        assertEquals("Basic quoted-printable encoding test",
            "1+1 =3D 2", encoded);
        assertEquals("Basic quoted-printable decoding test",
            plain, qpcodec.decode(encoded)); // calls a and b

    }
}
