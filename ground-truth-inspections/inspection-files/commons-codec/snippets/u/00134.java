public class BaseNCodec {
    /**
     * Calculates the amount of space needed to encode the supplied array.
     *
     * @param pArray byte[] array which will later be encoded
     *
     * @return amount of space needed to encoded the supplied array.
     * Returns a long since a max-len array will require &gt; Integer.MAX_VALUE
     */
    public long getEncodedLength(final byte[] pArray) {  // definition of a? ; not defined in Base64
        // Calculate non-chunked size - rounded up to allow for padding
        // cast to long is needed to avoid possibility of overflow
        long len = ((pArray.length + unencodedBlockSize-1)  / unencodedBlockSize) * (long) encodedBlockSize;
        if (lineLength > 0) { // We're using chunking
            // Round up to nearest multiple
            len += ((len + lineLength-1) / lineLength) * chunkSeparatorLength;
        }
        return len;
    }

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
    public byte[] encode(final byte[] pArray, final int offset, final int length) { // definition of b?
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

}

public class Base64Codec13Test {
    @Test
    public void testStaticEncode() throws EncoderException {
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                final byte[] base64 = utf8(STRINGS[i]);
                final byte[] binary = BYTES[i];
                final boolean b = Arrays.equals(base64, Base64.encodeBase64(binary));
                assertTrue("static Base64.encodeBase64() test-" + i, b);
            }
        }
    }

}
