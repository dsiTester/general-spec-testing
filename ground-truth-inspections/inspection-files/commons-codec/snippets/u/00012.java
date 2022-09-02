public class BaseNCodec {
    /**
     * Decodes an Object using the Base-N algorithm. This method is provided in order to satisfy the requirements of
     * the Decoder interface, and will throw a DecoderException if the supplied object is not of type byte[] or String.
     *
     * @param obj
     *            Object to decode
     * @return An object (of type byte[]) containing the binary data which corresponds to the byte[] or String
     *         supplied.
     * @throws DecoderException
     *             if the parameter supplied is not of type byte[]
     */
    @Override
    public Object decode(final Object obj) throws DecoderException { // definition of a (Base16Codec doesn't override this decode() method
        if (obj instanceof byte[]) {
            return decode((byte[]) obj); // call to b
        }
        if (obj instanceof String) {
            return decode((String) obj);
        }
        throw new DecoderException("Parameter supplied to Base-N decode is not a byte[] or a String");
    }

    /**
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    @Override
    public byte[] decode(final byte[] pArray) {
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
}

public class Base16Test {
    @Test
    public void testObjectDecodeWithValidParameter() throws Exception {
        final String original = "Hello World!";
        final Object o = new Base16().encode(original.getBytes(CHARSET_UTF8));

        final Base16 b16 = new Base16();
        final Object oDecoded = b16.decode(o); // call to a
        final byte[] baDecoded = (byte[]) oDecoded;
        final String dest = new String(baDecoded); // NullPointerException here

        assertEquals("dest string does not equal original", original, dest);
    }
}
