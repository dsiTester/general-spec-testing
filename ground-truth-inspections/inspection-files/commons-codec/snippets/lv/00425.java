public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    @Override
    protected String getEncoding() { // used implementation of a
        return "Q";
    }

    @Override
    protected byte[] doEncoding(final byte[] bytes) { // used implementation of b
        if (bytes == null) {
            return null;
        }
        final byte[] data = QuotedPrintableCodec.encodeQuotedPrintable(PRINTABLE_CHARS, bytes);
        if (this.encodeBlanks) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == SPACE) {
                    data[i] = UNDERSCORE;
                }
            }
        }
        return data;
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
    public String encode(final String sourceStr) throws EncoderException {
        if (sourceStr == null) {
            return null;
        }
        return encode(sourceStr, getCharset()); // calls a and b
    }

    public String encode(final String sourceStr, final Charset sourceCharset) throws EncoderException {
        if (sourceStr == null) {
            return null;
        }
        return encodeText(sourceStr, sourceCharset); // calls a and b
    }

}

abstract class RFC1522Codec {

    /**
     * Returns the codec name (referred to as encoding in the RFC 1522).
     *
     * @return name of the codec
     */
    protected abstract String getEncoding(); // a

    /**
     * Encodes an array of bytes using the defined encoding scheme.
     *
     * @param bytes
     *            Data to be encoded
     * @return A byte array containing the encoded data
     * @throws EncoderException
     *             thrown if the Encoder encounters a failure condition during the encoding process.
     */
    protected abstract byte[] doEncoding(byte[] bytes) throws EncoderException; // b

    protected String encodeText(final String text, final Charset charset) throws EncoderException {
        if (text == null) {
            return null;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(PREFIX);
        buffer.append(charset);
        buffer.append(SEP);
        buffer.append(this.getEncoding()); // call to a
        buffer.append(SEP);
        buffer.append(StringUtils.newStringUsAscii(this.doEncoding(text.getBytes(charset)))); // call to b
        buffer.append(POSTFIX);
        return buffer.toString();
    }

}

public class QCodecTest {

    @Test
    public void testEncodeObjects() throws Exception {
        final QCodec qcodec = new QCodec();
        final String plain = "1+1 = 2"; // replacement value?
        final String encoded = (String) qcodec.encode((Object) plain); // calls a and b
        assertEquals("Basic Q encoding test",
            "=?UTF-8?Q?1+1 =3D 2?=", encoded); // this assertion fails

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
