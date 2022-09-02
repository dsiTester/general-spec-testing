public abstract class BaseNCodec {
    /**
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    @Override
    public byte[] decode(final byte[] pArray) { // definition of a; not defined in Base64
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // call to b
        ...
        return result;
    }

    // package protected for access from I/O streams
    abstract void decode(byte[] pArray, int i, int length, Context context); // b
}

public class BCodec {
    @Override
    protected byte[] doDecoding(final byte[] bytes) { // indirectly called from test
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes);
    }

}

public class BCodecTest {
    @Test
    public void testBase64ImpossibleSamplesStrict() throws DecoderException {
        final BCodec codec = new BCodec(StandardCharsets.UTF_8, CodecPolicy.STRICT);
        Assert.assertTrue(codec.isStrictDecoding());
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            try {
                codec.decode(s); // calls a and b
                fail("Expected an exception for impossible case");
            } catch (final DecoderException ex) {
                // expected
            }
        }
    }

}
