public abstract class BaseNCodec {
    /**
     * Returns the decoding behavior policy.
     *
     * <p>
     * The default is lenient. If the decoding policy is strict, then decoding will raise an
     * {@link IllegalArgumentException} if trailing bits are not part of a valid encoding. Decoding will compose
     * trailing bits into 8-bit bytes and discard the remainder.
     * </p>
     *
     * @return true if using strict decoding
     * @since 1.15
     */
    public CodecPolicy getCodecPolicy() { // definition of a
        return decodingPolicy;
    }

    @Override
    public byte[] decode(final byte[] pArray) { // called from test
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls b
        ...
        return result;
    }

}

public class Base16 extends BaseNCodec {

    /**
     * Validates whether decoding allows an entire final trailing character that cannot be
     * used for a complete byte.
     *
     * @throws IllegalArgumentException if strict decoding is enabled
     */
    private void validateTrailingCharacter() {
        if (isStrictDecoding()) { // definition of b
            throw new IllegalArgumentException("Strict decoding: Last encoded character is a valid base 16 alphabet" +
                    "character but not a possible encoding. " +
                    "Decoding requires at least two characters to create one byte.");
        }
    }

    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // called from BaseNCodec.decode()
        if (context.eof || length < 0) {
            context.eof = true;
            if (context.ibitWorkArea != 0) {
                validateTrailingCharacter(); // call to b
            }
            return;
        }
        ...
    }
}

public class Base16Test {
    @Test
    public void testLenientDecoding() {
        final String encoded = "aabbccdde";  // Note the trailing `e` which does not make up a hex-pair and so is only 1/2 byte

        final Base16 b16 = new Base16(true, CodecPolicy.LENIENT);
        assertEquals(CodecPolicy.LENIENT, b16.getCodecPolicy()); // call to a; assertion fails here

        final byte[] decoded = b16.decode(StringUtils.getBytesUtf8(encoded)); // calls b
        assertArrayEquals(new byte[] {(byte)0xaa, (byte)0xbb, (byte)0xcc, (byte)0xdd}, decoded);
    }
}
