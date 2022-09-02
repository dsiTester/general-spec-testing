public abstract class BaseNCodec {
    public byte[] decode(final String pArray) { // called from test
        return decode(StringUtils.getBytesUtf8(pArray)); // calls b
    }

    @Override
    public byte[] decode(final byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls b
        ...
    }
}

public class Base32 extends BaseNCodec {
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

    /**
     * Validates whether decoding allows final trailing characters that cannot be
     * created during encoding.
     *
     * @throws IllegalArgumentException if strict decoding is enabled
     */
    private void validateTrailingCharacters() { // definition of b
        if (isStrictDecoding()) {
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character(s) (before the paddings if any) are valid " +
                "base 32 alphabet but not a possible encoding. " +
                "Decoding requires either 2, 4, 5, or 7 trailing 5-bit characters to create bytes.");
        }
    }

    public Base32(final int lineLength, final byte[] lineSeparator, final boolean useHex,
                  final byte padding, final CodecPolicy decodingPolicy) { // calls a
        super(BYTES_PER_UNENCODED_BLOCK, BYTES_PER_ENCODED_BLOCK, lineLength,
                lineSeparator == null ? 0 : lineSeparator.length, padding, decodingPolicy);
        ...
        if (isInAlphabet(padding) || isWhiteSpace(padding)) { // call to a
            throw new IllegalArgumentException("pad must not be in alphabet or whitespace");
        }
    }

    @Override
    void decode(final byte[] input, int inPos, final int inAvail, final Context context) { // called from BaseNCodec.decode()
        if (context.eof && context.modulus > 0) { // called from BaseNCodecInputStream.read()
            final byte[] buffer = ensureBufferSize(decodeSize, context);

            ...
            switch (context.modulus) {
                ...
            case 3 : // 15 bits, drop 7 and output 1 byte, or raise an exception
                validateTrailingCharacters(); // call to b
                // Not possible from a valid encoding but decode anyway
                buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 7) & MASK_8BITS);
                break;
                ...
            }
        }
        ...
    }
}

public class Base32Test {
    @Test
    public void testBase32ImpossibleChunked() {
        testImpossibleCases(
            new Base32(20, BaseNCodec.CHUNK_SEPARATOR, false, BaseNCodec.PAD_DEFAULT, CodecPolicy.STRICT),
            BASE32_IMPOSSIBLE_CASES_CHUNKED); // calls a
    }

    private void testImpossibleCases(final Base32 codec, final String[] impossible_cases) {
        for (final String impossible : impossible_cases) {
            try {
                codec.decode(impossible); // calls b
                fail();
            } catch (final IllegalArgumentException ex) {
                // expected
            }
        }
    }

}
