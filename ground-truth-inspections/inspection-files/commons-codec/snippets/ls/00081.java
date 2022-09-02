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
     * Validates whether decoding the final trailing character is possible in the context
     * of the set of possible base 32 values.
     *
     * <p>The character is valid if the lower bits within the provided mask are zero. This
     * is used to test the final trailing base-32 digit is zero in the bits that will be discarded.
     *
     * @param emptyBitsMask The mask of the lower bits that should be empty
     * @param context the context to be used
     *
     * @throws IllegalArgumentException if the bits being checked contain any non-zero value
     */
    private void validateCharacter(final long emptyBitsMask, final Context context) { // definition of b
        // Use the long bit work area
        if (isStrictDecoding() && (context.lbitWorkArea & emptyBitsMask) != 0) {
            throw new IllegalArgumentException(
                "Strict decoding: Last encoded character (before the paddings if any) is a valid " +
                "base 32 alphabet but not a possible encoding. " +
                "Expected the discarded bits from the character to be zero.");
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
    void decode(final byte[] input, int inPos, final int inAvail, final Context context) {
        if (context.eof && context.modulus > 0) { // called from BaseNCodecInputStream.read()
            final byte[] buffer = ensureBufferSize(decodeSize, context);

            ...
            switch (context.modulus) {
                ...
            case 5 : // 25 bits = 3*8 + 1
                validateCharacter(MASK_1BITS, context); // call to b
                ...
            }
        }
        ...
    }
}

public class BaseNCodecInputStream {
    @Override
    public int read() throws IOException { // called from test
        int r = read(singleByte, 0, 1); // calls b?
        ...
        return EOF;
    }

    @Override
    public int read(final byte array[], final int offset, final int len) throws IOException { // called from above
        ...
        while (readLen == 0) {
            if (!baseNCodec.hasData(context)) {
                final byte[] buf = new byte[doEncode ? 4096 : 8192];
                final int c = in.read(buf);
                if (doEncode) {
                    baseNCodec.encode(buf, 0, c, context);
                } else {
                    baseNCodec.decode(buf, 0, c, context); // calls b
                }
            }
            readLen = baseNCodec.readResults(array, offset, len, context);
        }
        return readLen;
    }
}

public class Base32InputStreamTest {
    /**
     * Tests skipping past the end of a stream.
     *
     * @throws Throwable
     *             for some failure scenarios.
     */
    @Test
    public void testAvailable() throws Throwable {
        final InputStream ins = new ByteArrayInputStream(StringUtils.getBytesIso8859_1(ENCODED_FOO));
        try (final Base32InputStream b32stream = new Base32InputStream(ins)) { // setup calls a and b?
            assertEquals(1, b32stream.available());
            assertEquals(3, b32stream.skip(10));
            // End of stream reached
            assertEquals(0, b32stream.available());
            assertEquals(-1, b32stream.read());
            assertEquals(-1, b32stream.read());
            assertEquals(0, b32stream.available());
        }
    }
}
