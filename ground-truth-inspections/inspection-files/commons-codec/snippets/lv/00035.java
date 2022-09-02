public abstract class BaseNCodec {
    /**
     * Ensure that the buffer has room for {@code size} bytes
     *
     * @param size minimum spare space required
     * @param context the context to be used
     * @return the buffer
     */
    protected byte[] ensureBufferSize(final int size, final Context context){ // definition of a; not defined in Base16
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
        decode(pArray, 0, pArray.length, context); // calls a
        decode(pArray, 0, EOF, context); // calls b
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }
}

public class Base16 extends BaseNCodec {

    /**
     * Validates whether decoding allows an entire final trailing character that cannot be
     * used for a complete byte.
     *
     * @throws IllegalArgumentException if strict decoding is enabled
     */
    private void validateTrailingCharacter() { // definition of b
        if (isStrictDecoding()) {
            throw new IllegalArgumentException("Strict decoding: Last encoded character is a valid base 16 alphabet" +
                    "character but not a possible encoding. " +
                    "Decoding requires at least two characters to create one byte."); // expected exception thrown here
        }
    }

    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) {
        if (context.eof || length < 0) {
            context.eof = true;
            if (context.ibitWorkArea != 0) {
                validateTrailingCharacter(); // calls b
            }
            return;
        }

        ...
        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // call to a
        ...
    }
}

public class Base16Test {
    @Test(expected=IllegalArgumentException.class)
    public void testStrictDecoding() {
        final String encoded = "aabbccdde";  // Note the trailing `e` which does not make up a hex-pair and so is only 1/2 byte

        final Base16 b16 = new Base16(true, CodecPolicy.STRICT);
        assertEquals(CodecPolicy.STRICT, b16.getCodecPolicy());
        b16.decode(StringUtils.getBytesUtf8(encoded)); // calls a and b
    }

}
