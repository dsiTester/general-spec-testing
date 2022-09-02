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
    public byte[] encode(final byte[] pArray, final int offset, final int length) { // definition of a; not defined in Base16
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        encode(pArray, offset, length, context); // call to b
        ...
        return buf;
    }

    // package protected for access from I/O streams
    abstract void encode(byte[] pArray, int i, int length, Context context); // b
}

public class Base16Test {
    @Test(expected = IllegalArgumentException.class) // Assertion fails because this exception was not thrown
    public void checkEncodeLengthBounds() {
        final Base16 base16 = new Base16();
        base16.encode(new byte[10], 0, 1 << 30); // call to a
    }
}
