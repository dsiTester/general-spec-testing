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
            return decode((byte[]) obj);
        }
        if (obj instanceof String) {
            return decode((String) obj); // call to b
        }
        throw new DecoderException("Parameter supplied to Base-N decode is not a byte[] or a String");
    }

    /**
     * Decodes a String containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A String containing Base-N character data
     * @return a byte array containing binary data
     */
    public byte[] decode(final String pArray) { // definition of b
        return decode(StringUtils.getBytesUtf8(pArray));
    }
}

public class Base16Test {
    @Test
    public void testStringToByteVariations() throws DecoderException {
        final Base16 base16 = new Base16();
        final String s1 = "48656C6C6F20576F726C64";
        final String s2 = "";
        final String s3 = null;

        ...
        assertEquals("StringToByte Hello World", "Hello World",
                StringUtils.newStringUtf8((byte[]) new Base16().decode((Object) s1))); // call to a; assertion fails here
        ...
    }

}
