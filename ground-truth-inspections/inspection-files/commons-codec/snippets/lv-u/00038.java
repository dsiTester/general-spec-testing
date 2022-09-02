public class Base16 extends BaseNCodec {

    /**
     * Ensure that the buffer has room for {@code size} bytes
     *
     * @param size minimum spare space required
     * @param context the context to be used
     * @return the buffer
     */
    protected byte[] ensureBufferSize(final int size, final Context context){ // definition of a
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
    void encode(final byte[] data, final int offset, final int length, final Context context) {
        ...

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

public abstract class BaseNCodec implements BinaryEncoder, BinaryDecoder {

    /**
     * Extracts buffered data into the provided byte[] array, starting at position bPos, up to a maximum of bAvail
     * bytes. Returns how many bytes were actually extracted.
     * <p>
     * Package protected for access from I/O streams.
     *
     * @param b
     *            byte[] array to extract the buffered data into.
     * @param bPos
     *            position in byte[] array to start extraction at.
     * @param bAvail
     *            amount of bytes we're allowed to extract. We may extract fewer (if fewer are available).
     * @param context
     *            the context to be used
     * @return The number of bytes successfully extracted into the provided byte[] array.
     */
    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) { // definition of b
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail);
            System.arraycopy(context.buffer, context.readPos, b, bPos, len);
            context.readPos += len;
            if (context.readPos >= context.pos) {
                context.buffer = null; // so hasData() will return false, and this method can return -1
            }
            return len;
        }
        return context.eof ? EOF : 0;
    }

    @Override
    public byte[] encode(final byte[] pArray) { // called from unknown test
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        return encode(pArray, 0, pArray.length);
    }

    public byte[] encode(final byte[] pArray, final int offset, final int length) { // called from validated test; called from above in unknown test
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        encode(pArray, offset, length, context); // calls a
        encode(pArray, offset, EOF, context); // Notify encoder of EOF.
        final byte[] buf = new byte[context.pos - context.readPos];
        readResults(buf, 0, buf.length, context); // call to b
        return buf;
    }
}

public class Base16Test {
    @Test
    public void testBase16AtBufferEnd() { // validated test
        testBase16InBuffer(100, 0);
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

    @Test
    public void testBase16() {  // unknown verdict test
        final String content = "Hello World";
        final byte[] encodedBytes = new Base16().encode(StringUtils.getBytesUtf8(content)); // calls a and b
        final String encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertEquals("encoding hello world", "48656C6C6F20576F726C64", encodedContent);

        final byte[] decodedBytes = new Base16().decode(encodedBytes); // calls a and b
        final String decodedContent = StringUtils.newStringUtf8(decodedBytes);
        assertEquals("decoding hello world", content, decodedContent);
    }

}
