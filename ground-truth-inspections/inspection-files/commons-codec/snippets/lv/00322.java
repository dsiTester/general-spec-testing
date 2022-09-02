public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
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
    protected String getEncoding() { // used implementation of b
        return "B";
    }

    @Override
    public Object encode(final Object value) throws EncoderException { // called from test
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return encode((String) value); // calls a and b
        }
        throw new EncoderException("Objects of type " +
              value.getClass().getName() +
              " cannot be encoded using BCodec");
    }

    @Override
    public String encode(final String strSource) throws EncoderException {
        if (strSource == null) {
            return null;
        }
        return encode(strSource, this.getCharset()); // call to a; encode() calls RFC1522Codec.encodeText, which throws exception
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
        ...
        buffer.append(this.getEncoding()); // call to b
        ...
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // NullPointerException here
        buffer.append(POSTFIX);
        return buffer.toString();
    }

}

public class BCodecTest {
    @Test
    public void testEncodeObjects() throws Exception {
        final BCodec bcodec = new BCodec();
        final String plain = "what not";
        final String encoded = (String) bcodec.encode((Object) plain); // calls a and b?

        assertEquals("Basic B encoding test", "=?UTF-8?B?d2hhdCBub3Q=?=", encoded);

        final Object result = bcodec.encode((Object) null);
        assertEquals("Encoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            bcodec.encode(dObj);
            fail("Trying to url encode a Double object should cause an exception.");
        } catch (final EncoderException ee) {
            // Exception expected, test segment passes.
        }
    }
}
