public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {

    @Override
    protected String getEncoding() { // used implementation of b
        return "Q";
    }

    /**
     * Decodes a quoted-printable string into its original form. Escaped characters are converted back to their original
     * representation.
     *
     * @param str
     *            quoted-printable string to convert into its original form
     * @return original string
     * @throws DecoderException
     *             A decoder exception is thrown if a failure condition is encountered during the decode process.
     */
    @Override
    public String decode(final String str) throws DecoderException { // definition of b
        if (str == null) {
            return null;
        }
        try {
            return decodeText(str);
        } catch (final UnsupportedEncodingException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    @Override
    public String encode(final String sourceStr) throws EncoderException { // called from test
        if (sourceStr == null) {
            return null;
        }
        return encode(sourceStr, getCharset()); // call to a
    }

}

abstract class RFC1522Codec {
    protected String encodeText(final String text, final Charset charset) throws EncoderException {
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
            "=?UTF-8?Q?=3D Hello there =3D=0D=0A?=", encoded); // this assertion failed
        assertEquals("Basic Q decoding test",
            plain, qcodec.decode(encoded)); // call to b
    }
}
