public class BCodec extends RFC1522Codec implements StringEncoder, StringDecoder {
    @Override
    protected byte[] doDecoding(final byte[] bytes) { // definition of a
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes);
    }

    @Override
    protected byte[] doEncoding(final byte[] bytes) { // definition of b
        if (bytes == null) {
            return null;
        }
        return Base64.encodeBase64(bytes);
    }
}

public class BCodecTest {
    @Test
    public void testNullInput() throws Exception {
        final BCodec bcodec = new BCodec();
        assertNull(bcodec.doDecoding(null)); // call to a
        assertNull(bcodec.doEncoding(null)); // call to b
    }

}
