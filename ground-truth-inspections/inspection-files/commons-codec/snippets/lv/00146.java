public abstract class BaseNCodec {
    abstract void decode(byte[] pArray, int i, int length, Context context); // a

    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of b
        return DEFAULT_BUFFER_SIZE;
    }

    @Override
    public byte[] decode(final byte[] pArray) { // called from test
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context);
        decode(pArray, 0, EOF, context); // Notify decoder of EOF.
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){ // called from implementation of a
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to b
            ...
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }

}

public class Base16 {
    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // used implementation of a
        ...

        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // calls b

        ...
    }

}

public class Base16Test {
    @Test(expected=RuntimeException.class)
    public void testCodec68() {
        final byte[] x = new byte[] { 'n', 'H', '=', '=', (byte) 0x9c };
        final Base16 b16 = new Base16();
        b16.decode(x);
    }
}
