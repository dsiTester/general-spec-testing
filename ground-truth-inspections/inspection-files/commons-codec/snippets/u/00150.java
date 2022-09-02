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
        readResults(buf, 0, buf.length, context); // calls b
        return buf;
    }

    /**
     * Returns the amount of buffered data available for reading.
     *
     * @param context the context to be used
     * @return The amount of buffered data available for reading.
     */
    int available(final Context context) {  // definition of b
        return context.buffer != null ? context.pos - context.readPos : 0;
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

    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) { // called from a
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail); // call to b
            System.arraycopy(context.buffer, context.readPos, b, bPos, len);
            context.readPos += len;
            if (context.readPos >= context.pos) {
                context.buffer = null; // so hasData() will return false, and this method can return -1
            }
            return len;
        }
        return context.eof ? EOF : 0;
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
