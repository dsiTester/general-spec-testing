public abstract class BaseNCodec {
    /**
     * Encodes a byte[] containing binary data, into a byte[] containing characters in the alphabet.
     *
     * @param pArray
     *            a byte array containing binary data
     * @return A byte array containing only the base N alphabetic character data
     */
    @Override
    public byte[] encode(final byte[] pArray) { // definition of a ; not defined in Base64
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        return encode(pArray, 0, pArray.length); // call to b
    }

    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of b
        return DEFAULT_BUFFER_SIZE;
    }

    public byte[] encode(final byte[] pArray, final int offset, final int length) { // called from a
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        encode(pArray, offset, length, context); // calls b
        ...
        return buf;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){ // called from Base64.encode()
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())];
            ...
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }

}

public class Base64 extends BaseNCodec {
    public static byte[] encodeBase64(final byte[] binaryData, final boolean isChunked,
                                      final boolean urlSafe, final int maxResultSize) {
        ...
        return b64.encode(binaryData); // call to a
    }

    @Override
    void encode(final byte[] in, int inPos, final int inAvail, final Context context) { // called from BaseNCodec.encode()
        ...
        if (inAvail < 0) {
            context.eof = true;
            if (0 == context.modulus && lineLength == 0) {
                return; // no leftovers to process and not using chunking
            }
            final byte[] buffer = ensureBufferSize(encodeSize, context); // calls b
            ...
        }
        ...
    }
}

public class Base64Codec13Test {
    @Test
    public void testStaticEncode() throws EncoderException {
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                final byte[] base64 = utf8(STRINGS[i]);
                final byte[] binary = BYTES[i];
                final boolean b = Arrays.equals(base64, Base64.encodeBase64(binary)); // calls a and b
                assertTrue("static Base64.encodeBase64() test-" + i, b); // test fails here
            }
        }
    }

}
