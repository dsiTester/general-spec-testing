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
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to b
            context.pos = 0;
            context.readPos = 0;

            // Overflow-conscious:
            // x + y > z  ==  x + y - z > 0
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }

    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() {
        return DEFAULT_BUFFER_SIZE;
    }

    public byte[] encode(final byte[] pArray, final int offset, final int length) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        encode(pArray, offset, length, context); // calls a
        ...
        return buf;
    }

}

public class Base16 {
    @Override
    void encode(final byte[] data, final int offset, final int length, final Context context) {
        ...
        // NOTE: one can call b before a by uncommenting the following
        // System.out.println(available(context));
        final byte[] buffer = ensureBufferSize(size, context); // call to a

        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            final int value = data[i];
            final int high = (value >> BITS_PER_ENCODED_BYTE) & MASK_4BITS;
            final int low = value & MASK_4BITS;
            buffer[context.pos++] = encodeTable[high];
            buffer[context.pos++] = encodeTable[low];
        }
    }
}

public class Base16Test {
    @Test
    public void testBase16AtBufferStart() {
        testBase16InBuffer(0, 100); // calls a and b
    }

    private void testBase16InBuffer(final int startPasSize, final int endPadSize) {
        final String content = "Hello World";
        final String encodedContent;
        final byte[] bytesUtf8 = StringUtils.getBytesUtf8(content);
        byte[] buffer = ArrayUtils.addAll(bytesUtf8, new byte[endPadSize]);
        buffer = ArrayUtils.addAll(new byte[startPasSize], buffer);
        final byte[] encodedBytes = new Base16().encode(buffer, startPasSize, bytesUtf8.length); // calls a and b
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertEquals("encoding hello world", "48656C6C6F20576F726C64", encodedContent); // assertion fails here
    }

}
