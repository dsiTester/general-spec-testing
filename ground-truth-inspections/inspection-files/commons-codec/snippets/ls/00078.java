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
    public CodecPolicy getCodecPolicy() { // definition of b; not defined in Base32
        return decodingPolicy;
    }
}

public class Base32 {
    /**
     * Returns whether or not the {@code octet} is in the Base32 alphabet.
     *
     * @param octet
     *            The value to test
     * @return {@code true} if the value is defined in the the Base32 alphabet {@code false} otherwise.
     */
    @Override
    public boolean isInAlphabet(final byte octet) { // definition of a
        return octet >= 0 && octet < decodeTable.length && decodeTable[octet] != -1;
    }

    public Base32(final int lineLength, final byte[] lineSeparator, final boolean useHex,
                  final byte padding, final CodecPolicy decodingPolicy) {
        super(BYTES_PER_UNENCODED_BLOCK, BYTES_PER_ENCODED_BLOCK, lineLength,
                lineSeparator == null ? 0 : lineSeparator.length, padding, decodingPolicy);
        ...
        if (isInAlphabet(padding) || isWhiteSpace(padding)) { // call to a
            throw new IllegalArgumentException("pad must not be in alphabet or whitespace");
        }
    }
}

public class Base32Test {
    @Test
    public void testBase32DecodingOfTrailing10Bits() {
        assertBase32DecodingOfTrailingBits(10);
    }

    private static void assertBase32DecodingOfTrailingBits(final int nbits) {
        // Requires strict decoding
        final Base32 codec = new Base32(0, null, false, BaseNCodec.PAD_DEFAULT, CodecPolicy.STRICT); // calls a
        assertTrue(codec.isStrictDecoding());
        assertEquals(CodecPolicy.STRICT, codec.getCodecPolicy()); // call to b
        // A lenient decoder should not re-encode to the same bytes
        final Base32 defaultCodec = new Base32(); // calls a
        assertFalse(defaultCodec.isStrictDecoding());
        assertEquals(CodecPolicy.LENIENT, defaultCodec.getCodecPolicy()); // call to b
    }
}
