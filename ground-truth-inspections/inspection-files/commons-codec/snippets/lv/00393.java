public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

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
     *
     * @param sourceStr
     *            string to convert to quoted-printable form
     * @param sourceCharset
     *            the Charset for sourceStr
     * @return quoted-printable string
     * @throws EncoderException
     *             thrown if a failure condition is encountered during the encoding process.
     * @since 1.7
     */
    public String encode(final String sourceStr, final Charset sourceCharset) throws EncoderException { // definition of b
        if (sourceStr == null) {
            return null;
        }
        return encodeText(sourceStr, sourceCharset); // throws NullPointerException
    }

    @Override
    public String encode(final String sourceStr) throws EncoderException { // called from test
        if (sourceStr == null) {
            return null;
        }
        return encode(sourceStr, getCharset()); // call to a; call to b
    }

}

abstract class RFC1522Codec {

    protected String encodeText(final String text, final Charset charset) throws EncoderException { // called from b
        ...
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // NullPointerException thrown here
        buffer.append(POSTFIX);
        return buffer.toString();
    }

}

public class QCodecTest {
    @Test
    public void testBasicEncodeDecode() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "= Hello there =\r\n";
        final String encoded = qcodec.encode(plain); // calls a
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=", encoded);
        assertEquals("Basic Q decoding test",
            plain, qcodec.decode(encoded)); // calls b
    }
}
