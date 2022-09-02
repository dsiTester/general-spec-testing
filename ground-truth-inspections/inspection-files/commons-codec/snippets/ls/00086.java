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
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of b
        return DEFAULT_BUFFER_SIZE;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to b
            ...
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }

}

public class Base32OutputStreamTest {
    @Test
    public void testStrictDecoding() throws Exception {
        for (final String s : Base32Test.BASE32_IMPOSSIBLE_CASES) {
            final byte[] encoded = StringUtils.getBytesUtf8(s);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            Base32OutputStream out = new Base32OutputStream(bout, false);
            // Default is lenient decoding; it should not throw
            assertFalse(out.isStrictDecoding());
            out.write(encoded);
            out.close();
            assertTrue(bout.size() > 0);

            // Strict decoding should throw
            bout = new ByteArrayOutputStream();
            out = new Base32OutputStream(bout, false, 0, null, CodecPolicy.STRICT);
            assertTrue(out.isStrictDecoding());
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
