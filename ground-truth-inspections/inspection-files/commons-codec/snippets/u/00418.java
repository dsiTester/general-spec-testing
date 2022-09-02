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
     * Encodes an array of bytes into an array of quoted-printable 7-bit characters. Unsafe characters are escaped.
     * <p>
     * Depending on the selection of the {@code strict} parameter, this function either implements the full ruleset
     * or only a subset of quoted-printable encoding specification (rule #1 and rule #2) as defined in
     * RFC 1521 and is suitable for encoding binary data and unformatted text.
     *
     * @param bytes
     *            array of bytes to be encoded
     * @return array of bytes containing quoted-printable data
     */
    @Override
    public byte[] encode(final byte[] bytes) { // definition of b
        return encodeQuotedPrintable(PRINTABLE_CHARS, bytes, strict);
    }

    @Override
    public String encode(final String sourceStr) throws EncoderException { // called from test
        return this.encode(sourceStr, getCharset()); // call to a; calls b
    }

    public String encode(final String sourceStr, final Charset sourceCharset) {
        if (sourceStr == null) {
            return null;
        }
        return StringUtils.newStringUsAscii(this.encode(sourceStr.getBytes(sourceCharset))); // call to b; NullPointerException here
    }


}

public class QuotedPrintableCodecTest {
    @Test
    public void testFinalBytes() throws Exception {
        // whitespace, but does not need to be encoded
        final String plain ="This is a example of a quoted=printable text file. There is no tt";
        final String expected = "This is a example of a quoted=3Dprintable text file. There is no tt";

        assertEquals(expected, new QuotedPrintableCodec(true).encode(plain)); // calls a and b
    }
}
