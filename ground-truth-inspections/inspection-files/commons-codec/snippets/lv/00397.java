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

    @Override
    protected String getEncoding() { // definition of b
        return "Q";
    }

    @Override
    public Object encode(final Object obj) throws EncoderException { // called from test
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return encode((String) obj); // calls a and b
        }
        throw new EncoderException("Objects of type " +
              obj.getClass().getName() +
              " cannot be encoded using Q codec");
    }

    @Override
    public String encode(final String sourceStr) throws EncoderException { // called from above
        if (sourceStr == null) {
            return null;
        }
        return encode(sourceStr, getCharset()); // call to a; calls b
    }

    public String encode(final String sourceStr, final Charset sourceCharset) throws EncoderException { // called from above
        if (sourceStr == null) {
            return null;
        }
        return encodeText(sourceStr, sourceCharset); // calls b; throws NullPointerException
    }

}

abstract class RFC1522Codec {

    /**
     * Returns the codec name (referred to as encoding in the RFC 1522).
     *
     * @return name of the codec
     */
    protected abstract String getEncoding(); // b

    protected String encodeText(final String text, final Charset charset) throws EncoderException {
        if (text == null) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(PREFIX);
        buffer.append(charset);
        buffer.append(SEP);
        buffer.append(this.getEncoding()); // call to b
        buffer.append(SEP);
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // NullPointerException here
        buffer.append(POSTFIX);
        return buffer.toString();
    }

}

public class QCodecTest {
    @Test
    public void testEncodeObjects() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "1+1 = 2";
        final String encoded = (String) qcodec.encode((Object) plain); // calls a and b
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?1+1 =3D 2?=", encoded);

        final Object result = qcodec.encode((Object) null);
        assertEquals( "Encoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            qcodec.encode( dObj );
            fail( "Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            // Exception expected, test segment passes.
        }
    }
}
