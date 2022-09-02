public class BaseNCodec {
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
        ...
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

    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) { // called from a
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail); // call to b
            ...
            return len;
        }
        return context.eof ? EOF : 0;
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
        final byte[] encodedBytes = new Base16().encode(buffer, startPasSize, bytesUtf8.length); // call to a
        encodedContent = StringUtils.newStringUtf8(encodedBytes);
        assertEquals("encoding hello world", "48656C6C6F20576F726C64", encodedContent); // assertion fails here
    }

}
