public abstract class BaseNCodec {
    /**
     * Encodes an Object using the Base-N algorithm. This method is provided in order to satisfy the requirements of
     * the Encoder interface, and will throw an EncoderException if the supplied object is not of type byte[].
     *
     * @param obj
     *            Object to encode
     * @return An object (of type byte[]) containing the Base-N encoded data which corresponds to the byte[] supplied.
     * @throws EncoderException
     *             if the parameter supplied is not of type byte[]
     */
    @Override
    public Object encode(final Object obj) throws EncoderException { // definition of a; not defined in Base16
        if (!(obj instanceof byte[])) {
            throw new EncoderException("Parameter supplied to Base-N encode is not a byte[]");
        }
        return encode((byte[]) obj); // calls b
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

    @Override
    public byte[] encode(final byte[] pArray) { // called from a
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        return encode(pArray, 0, pArray.length); // calls b
    }

    public byte[] encode(final byte[] pArray, final int offset, final int length) {
        ...
        final byte[] buf = new byte[context.pos - context.readPos];
        readResults(buf, 0, buf.length, context); // calls b
        return buf;
    }

    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) {
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
    public void testKnownEncodings() {
        assertEquals("54686520717569636b2062726f776e20666f78206a756d706564206f76657220746865206c617a7920646f67732e", new String(
                new Base16(true).encode("The quick brown fox jumped over the lazy dogs.".getBytes(CHARSET_UTF8)))); // call to a
        assertEquals("497420776173207468652062657374206f662074696d65732c206974207761732074686520776f727374206f662074696d65732e", new String(
                new Base16(true).encode("It was the best of times, it was the worst of times.".getBytes(CHARSET_UTF8)))); // call to a
        assertEquals("687474703a2f2f6a616b617274612e6170616368652e6f72672f636f6d6d6d6f6e73",
                new String(new Base16(true).encode("http://jakarta.apache.org/commmons".getBytes(CHARSET_UTF8)))); // call to a
        assertEquals("4161426243634464456546664767486849694a6a4b6b4c6c4d6d4e6e4f6f50705171527253735474557556765777587859795a7a", new String(
                new Base16(true).encode("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz".getBytes(CHARSET_UTF8)))); // call to a
        assertEquals("7b20302c20312c20322c20332c20342c20352c20362c20372c20382c2039207d",
                new String(new Base16(true).encode("{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }".getBytes(CHARSET_UTF8)))); // call to a
        assertEquals("78797a7a7921", new String(new Base16(true).encode("xyzzy!".getBytes(CHARSET_UTF8)))); // call to a
    }

}
