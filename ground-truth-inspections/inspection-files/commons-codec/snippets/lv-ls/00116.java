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
    protected boolean containsAlphabetOrPad(final byte[] arrayOctet) { // definition of a; not defined in Base64
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
}

public class Base64 extends BaseNCodec {

    /**
     * Validates whether decoding the final trailing character is possible in the context
     * of the set of possible base 64 values.
     *
     * <p>The character is valid if the lower bits within the provided mask are zero. This
     * is used to test the final trailing base-64 digit is zero in the bits that will be discarded.
     *
     * @param emptyBitsMask The mask of the lower bits that should be empty
     * @param context the context to be used
     *
     * @throws IllegalArgumentException if the bits being checked contain any non-zero value
     */
    private void validateCharacter(final int emptyBitsMask, final Context context) { // definition of b
        if (isStrictDecoding() && (context.ibitWorkArea & emptyBitsMask) != 0) {
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 64 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
        }
    }

    public Base64(final int lineLength, final byte[] lineSeparator, final boolean urlSafe,
                  final CodecPolicy decodingPolicy) {
        ...
        // @see test case Base64Test.testConstructors()
        if (lineSeparator != null) {
            if (containsAlphabetOrPad(lineSeparator)) { // call to a
                final String sep = StringUtils.newStringUtf8(lineSeparator);
                throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + sep + "]");
            }
            ...
        } ...
    }

    @Override
    void decode(final byte[] in, int inPos, final int inAvail, final Context context) {
        ...

        // Two forms of EOF as far as base64 decoder is concerned: actual
        // EOF (-1) and first time '=' character is encountered in stream.
        // This approach makes the '=' padding characters completely optional.
        if (context.eof && context.modulus != 0) {
            final byte[] buffer = ensureBufferSize(decodeSize, context);

            // We have some spare bits remaining
            // Output all whole multiples of 8 bits and ignore the rest
            switch (context.modulus) {
//              case 0 : // impossible, as excluded above
                case 1 : // 6 bits - either ignore entirely, or raise an exception
                    validateTrailingCharacter();
                    break;
                case 2 : // 12 bits = 8 + 4
                    validateCharacter(MASK_4BITS, context); // call to b
                    ...
                    break;
                case 3 : // 18 bits = 8 + 8 + 2
                    validateCharacter(MASK_2BITS, context); // call to b
                    ...
                    break;
                default:
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
        }
    }

}

public class BCodec {
    @Override
    protected byte[] doDecoding(final byte[] bytes) { // eventually called from test
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes); // Base64() calls a, decode() calls b
    }

}

public class BCodecTest {
    @Test
    public void testBase64ImpossibleSamplesStrict() throws DecoderException { // validated test
        final BCodec codec = new BCodec(StandardCharsets.UTF_8, CodecPolicy.STRICT);
        Assert.assertTrue(codec.isStrictDecoding());
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            try {
                codec.decode(s); // calls a and b
                fail("Expected an exception for impossible case");
            } catch (final DecoderException ex) {
                // expected
            }
        }
    }

    @Test
    public void testBase64ImpossibleSamplesDefault() throws DecoderException { // invalidated test
        final BCodec codec = new BCodec();
        // Default encoding is lenient
        Assert.assertFalse(codec.isStrictDecoding());
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            codec.decode(s); // calls a and b
        }
    }
}
