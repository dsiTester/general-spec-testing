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

    public Base32(final int lineLength, final byte[] lineSeparator, final boolean useHex,
                  final byte padding, final CodecPolicy decodingPolicy) { // calls a
        super(BYTES_PER_UNENCODED_BLOCK, BYTES_PER_ENCODED_BLOCK, lineLength,
                lineSeparator == null ? 0 : lineSeparator.length, padding, decodingPolicy);
        ...
        if (isInAlphabet(padding) || isWhiteSpace(padding)) { // call to a
            throw new IllegalArgumentException("pad must not be in alphabet or whitespace");
        }
    }
}

public abstract class BaseNCodec {
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
}

public class BaseNCodecOutputStream {

    public boolean isStrictDecoding() { // called from test
        return baseNCodec.isStrictDecoding(); // call to b
    }
}

public class Base32OutputStream extends BaseNCodecOutputStream {
    public Base32OutputStream(final OutputStream out, final boolean doEncode) {
        super(out, new Base32(false), doEncode); // the new Base32() constructor transitively calls a
    }
}

public class Base32OutputStreamTest {
    @Test
    public void testStrictDecoding() throws Exception {
        for (final String s : Base32Test.BASE32_IMPOSSIBLE_CASES) {
            final byte[] encoded = StringUtils.getBytesUtf8(s);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Base32OutputStream out = new Base32OutputStream(bout, false); // calls a
            // Default is lenient decoding; it should not throw
            assertFalse(out.isStrictDecoding()); // calls b
            out.write(encoded);
            out.close();
            assertTrue(bout.size() > 0);

            // Strict decoding should throw
            bout = new ByteArrayOutputStream();
            out = new Base32OutputStream(bout, false, 0, null, CodecPolicy.STRICT);
            assertTrue(out.isStrictDecoding()); // calls b
            try {
                out.write(encoded);
                out.close();
                fail();
            } catch (final IllegalArgumentException ex) {
                // expected
            }
        }
    }
}
