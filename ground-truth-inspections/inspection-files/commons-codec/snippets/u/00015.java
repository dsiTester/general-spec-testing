public class BaseNCodec {
    /**
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    @Override
    public byte[] decode(final byte[] pArray) { // definition of a; a is not defined in Base16
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls b
        decode(pArray, 0, EOF, context); // Notify decoder of EOF.
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }

}

public class Base16 extends BaseNCodec {

    /**
     * Returns true if decoding behavior is strict. Decoding will raise an {@link IllegalArgumentException} if trailing
     * bits are not part of a valid encoding.
     *
     * <p>
     * The default is false for lenient decoding. Decoding will compose trailing bits into 8-bit bytes and discard the
     * remainder.
     * </p>
     *
     * @return true if using strict decoding
     * @since 1.15
     */
    public boolean isStrictDecoding() { // definition of b
        return decodingPolicy == CodecPolicy.STRICT;
    }

    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // called from a
        if (context.eof || length < 0) {
            context.eof = true;
            if (context.ibitWorkArea != 0) {
                validateTrailingCharacter();
            }
            return;
        }
        ...
    }

    private void validateTrailingCharacter() {
        if (isStrictDecoding()) { // call to b
            throw new IllegalArgumentException("Strict decoding: Last encoded character is a valid base 16 alphabet" +
                    "character but not a possible encoding. " +
                    "Decoding requires at least two characters to create one byte.");
        }
    }
}

public class Base16Test {
    @Test(expected=IllegalArgumentException.class) // Assertion fails because this is not thrown when DSI perturbs.
    public void testStrictDecoding() {
        final String encoded = "aabbccdde";  // Note the trailing `e` which does not make up a hex-pair and so is only 1/2 byte

        final Base16 b16 = new Base16(true, CodecPolicy.STRICT);
        assertEquals(CodecPolicy.STRICT, b16.getCodecPolicy());
        b16.decode(StringUtils.getBytesUtf8(encoded)); // call to a
    }
}
