// both a not defined in Base32
public abstract class BaseNCodec {
    /**
     * Tests a given byte array to see if it contains any characters within the alphabet or PAD.
     *
     * Intended for use in checking line-ending arrays
     *
     * @param arrayOctet
     *            byte array to test
     * @return {@code true} if any byte is a valid character in the alphabet or PAD; {@code false} otherwise
     */
    protected boolean containsAlphabetOrPad(final byte[] arrayOctet) { // definition of a
        if (arrayOctet == null) {
            return false;
        }
        for (final byte element : arrayOctet) {
            if (pad == element || isInAlphabet(element)) {
                return true;
            }
        }
        return false;
    }

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
        return result;
    }

}

public class Base32 extends BaseNCodec {

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
                  final byte padding, final CodecPolicy decodingPolicy) {
        super(BYTES_PER_UNENCODED_BLOCK, BYTES_PER_ENCODED_BLOCK, lineLength,
              lineSeparator == null ? 0 : lineSeparator.length, padding, decodingPolicy);
        ...
        if (lineLength > 0) {
            ...
            // Must be done after initializing the tables
            if (containsAlphabetOrPad(lineSeparator)) { // call to a
                final String sep = StringUtils.newStringUtf8(lineSeparator);
                throw new IllegalArgumentException("lineSeparator must not contain Base32 characters: [" + sep + "]");
            }
            this.encodeSize = BYTES_PER_ENCODED_BLOCK + lineSeparator.length;
            this.lineSeparator = lineSeparator.clone();
        }
        ...
    }

    @Override
    void decode(final byte[] input, int inPos, final int inAvail, final Context context) { // called from BaseNCodec.decode()
        ...
        switch (context.modulus) {
//              case 0 : // impossible, as excluded above
                case 1 : // 5 bits - either ignore entirely, or raise an exception
                    validateTrailingCharacters();
                case 2 : // 10 bits, drop 2 and output one byte
                    validateCharacter(MASK_2BITS, context);
                    buffer[context.pos++] = (byte) ((context.lbitWorkArea >> 2) & MASK_8BITS);
                    break;
                case 3 : // 15 bits, drop 7 and output 1 byte, or raise an exception
                    validateTrailingCharacters(); // call to b
                    ...
                ...
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
