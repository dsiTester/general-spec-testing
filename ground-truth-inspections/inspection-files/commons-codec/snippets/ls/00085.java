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

public class BaseNCodecOutputStream {
    /**
     * <p>
     * Encodes all of the provided data, starting at inPos, for inAvail bytes. Must be called at least twice: once with
     * the data to encode, and once with inAvail set to "-1" to alert encoder that EOF has been reached, so flush last
     * remaining bytes (if not multiple of 5).
     * </p>
     *
     * @param input
     *            byte[] array of binary data to Base32 encode.
     * @param inPos
     *            Position to start reading data from.
     * @param inAvail
     *            Amount of bytes available from input for encoding.
     * @param context the context to be used
     */
    @Override
    void encode(final byte[] input, int inPos, final int inAvail, final Context context) { // definition of b
        // package protected for access from I/O streams

        if (context.eof) {
            return;
        }
        // inAvail < 0 is how we're informed of EOF in the underlying data we're
        // encoding.
        if (inAvail < 0) {
            context.eof = true;
            if (0 == context.modulus && lineLength == 0) {
                return; // no leftovers to process and not using chunking
            }
            final byte[] buffer = ensureBufferSize(encodeSize, context);
            final int savedPos = context.pos;
            switch (context.modulus) { // % 5
                case 0 :
                    break;
                case 1 : // Only 1 octet; take top 5 bits then remainder
                    ...
                    break;
                case 3 : // 3 octets = 24 bits to use
                    ...
                    break;
                case 4 : // 4 octets = 32 bits to use
                    ...
                    break;
                default:
                    throw new IllegalStateException("Impossible modulus " + context.modulus);
            }
            context.currentLinePos += context.pos - savedPos; // keep track of current line position
            // if currentPos == 0 we are at the start of a line, so don't add CRLF
            if (lineLength > 0 && context.currentLinePos > 0){ // add chunk separator if required
                System.arraycopy(lineSeparator, 0, buffer, context.pos, lineSeparator.length);
                context.pos += lineSeparator.length;
            }
        } else {
            for (int i = 0; i < inAvail; i++) {
                ...
            }
        }
    }

    public void eof() throws IOException {
        // Notify encoder of EOF (-1).
        if (doEncode) {
            baseNCodec.encode(singleByte, 0, EOF, context); // call to b
        } else {
            baseNCodec.decode(singleByte, 0, EOF, context);
        }
    }
}

public class Base32OutputStreamTest {
    @Test
    public void testWriteOutOfBounds() throws Exception {
        final byte[] buf = new byte[1024];
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final Base32OutputStream out = new Base32OutputStream(bout)) {

            try {
                out.write(buf, -1, 1);
                fail("Expected Base32OutputStream.write(buf, -1, 1) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, 1, -1);
                fail("Expected Base32OutputStream.write(buf, 1, -1) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, buf.length + 1, 0);
                fail("Expected Base32OutputStream.write(buf, buf.length + 1, 0) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, buf.length - 1, 2);
                fail("Expected Base32OutputStream.write(buf, buf.length - 1, 2) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }
        }
    }
}
