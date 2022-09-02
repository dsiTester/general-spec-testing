public class QCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    @Override
    protected byte[] doDecoding(final byte[] bytes) throws DecoderException { // call to a
        if (bytes == null) {
            return null;
        }
        boolean hasUnderscores = false;
        for (final byte b : bytes) {
            if (b == UNDERSCORE) {
                hasUnderscores = true;
                break;
            }
        }
        if (hasUnderscores) {
            final byte[] tmp = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                final byte b = bytes[i];
                if (b != UNDERSCORE) {
                    tmp[i] = b;
                } else {
                    tmp[i] = SPACE;
                }
            }
            return QuotedPrintableCodec.decodeQuotedPrintable(tmp);
        }
        return QuotedPrintableCodec.decodeQuotedPrintable(bytes);
    }

    @Override
    protected byte[] doEncoding(final byte[] bytes) { // call to b
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
}

public class QCodecTest {
    @Test
    public void testNullInput() throws Exception {
        final QCodec qcodec = new QCodec();
        assertNull(qcodec.doDecoding(null)); // call to a
        assertNull(qcodec.doEncoding(null)); // call to b
    }
}
