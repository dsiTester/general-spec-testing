public abstract class BaseNCodec {
    /**
     * Returns the decoding behavior policy.
     *
     * <p>
     * The default is lenient. If the decoding policy is strict, then decoding will raise an
     * {@link IllegalArgumentException} if trailing bits are not part of a valid encoding. Decoding will compose
     * trailing bits into 8-bit bytes and discard the remainder.
     * </p>
     *
     * @return true if using strict decoding
     * @since 1.15
     */
    public CodecPolicy getCodecPolicy() { // definition of a
        return decodingPolicy;
    }

    /**
     * Ensure that the buffer has room for {@code size} bytes
     *
     * @param size minimum spare space required
     * @param context the context to be used
     * @return the buffer
     */
    protected byte[] ensureBufferSize(final int size, final Context context){ // definition of b
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())];
            context.pos = 0;
            context.readPos = 0;

            // Overflow-conscious:
            // x + y > z  ==  x + y - z > 0
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }

    @Override
    public byte[] decode(final byte[] pArray) { // called from test
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls b
        ...
        return result;
    }
}

public class Base16 extends BaseNCodec {
    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) {
        ...
        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // call to b
        ...
    }
}

public class Base16Test {
    @Test
    public void testLenientDecoding() {
        final String encoded = "aabbccdde";  // Note the trailing `e` which does not make up a hex-pair and so is only 1/2 byte

        final Base16 b16 = new Base16(true, CodecPolicy.LENIENT);
        assertEquals(CodecPolicy.LENIENT, b16.getCodecPolicy()); // call to a; assertion fails here

        final byte[] decoded = b16.decode(StringUtils.getBytesUtf8(encoded)); // calls b
        assertArrayEquals(new byte[] {(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd}, decoded);
    }
}
