public class BaseNCodec {
    /**
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    @Override
    public byte[] decode(final byte[] pArray) { // definition of a; a is not defined in Base16
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls b
        decode(pArray, 0, EOF, context); // Notify decoder of EOF.
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }

    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of b
        return DEFAULT_BUFFER_SIZE;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){ // called from Base16.decode()
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to b
            ...
        } ...
        return context.buffer;
    }
}

public class Base16 extends BaseNCodec {
    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) {
        ...
        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // calls b
        ...
    }
}

public class Base16Test {
    @Test
    public void testLenientDecoding() {
        final String encoded = "aabbccdde";  // Note the trailing `e` which does not make up a hex-pair and so is only 1/2 byte

        final Base16 b16 = new Base16(true, CodecPolicy.LENIENT);
        assertEquals(CodecPolicy.LENIENT, b16.getCodecPolicy());

        final byte[] decoded = b16.decode(StringUtils.getBytesUtf8(encoded)); // call to a
        assertArrayEquals(new byte[] {(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd}, decoded); // assertion fails here
    }
}
