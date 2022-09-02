public abstract class BaseNCodec {
    /**
     * Encodes a byte[] containing binary data, into a byte[] containing
     * characters in the alphabet.
     *
     * @param pArray
     *            a byte array containing binary data
     * @param offset
     *            initial offset of the subarray.
     * @param length
     *            length of the subarray.
     * @return A byte array containing only the base N alphabetic character data
     * @since 1.11
     */
    public byte[] encode(final byte[] pArray, final int offset, final int length) { // definition of a
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        encode(pArray, offset, length, context);
        encode(pArray, offset, EOF, context); // Notify encoder of EOF.
        final byte[] buf = new byte[context.pos - context.readPos];
        readResults(buf, 0, buf.length, context);
        return buf;
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

    public String encodeToString(final byte[] pArray) {
        return StringUtils.newStringUtf8(encode(pArray)); // calls a and b
    }

    @Override
    public byte[] encode(final byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        return encode(pArray, 0, pArray.length); // call to a
    }
}

public class Base16 extends BaseNCodec {
    @Override
    void encode(final byte[] data, final int offset, final int length, final Context context) { // called from a
        ...
        final byte[] buffer = ensureBufferSize(size, context); // call to b
        ...
    }
}

public class Base16Test {
    @Test
    public void testByteToStringVariations() throws DecoderException {
        final Base16 base16 = new Base16();
        final byte[] b1 = StringUtils.getBytesUtf8("Hello World"); // replacement value?
        final byte[] b2 = new byte[0];
        final byte[] b3 = null;

        assertEquals("byteToString Hello World", "48656C6C6F20576F726C64", base16.encodeToString(b1)); // calls a and b; assertion failed
        assertEquals("byteToString static Hello World", "48656C6C6F20576F726C64", StringUtils.newStringUtf8(new Base16().encode(b1)));
        assertEquals("byteToString \"\"", "", base16.encodeToString(b2));
        assertEquals("byteToString static \"\"", "", StringUtils.newStringUtf8(new Base16().encode(b2)));
        assertEquals("byteToString null", null, base16.encodeToString(b3));
        assertEquals("byteToString static null", null, StringUtils.newStringUtf8(new Base16().encode(b3)));
    }
}
