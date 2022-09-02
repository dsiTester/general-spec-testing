// both a and b are not defined in Base32
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

public class Base32 extends BaseNCodec {
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
    }

    @Override
    void encode(final byte[] input, int inPos, final int inAvail, final Context context) {
        // package protected for access from I/O streams
        ...
        if (inAvail < 0) {
            context.eof = true;
            if (0 == context.modulus && lineLength == 0) {
                return; // no leftovers to process and not using chunking
            }
            final byte[] buffer = ensureBufferSize(encodeSize, context); // calls b
            ...
        }
    }
}

public class Base32OutputStreamTest {
    /**
     * Test the Base32OutputStream implementation against empty input.
     *
     * @throws Exception
     *             for some failure scenarios.
     */
    @Test
    public void testBase32EmptyOutputStreamMimeChunkSize() throws Exception {
        testBase32EmptyOutputStream(BaseNCodec.MIME_CHUNK_SIZE);
    }

    private void testBase32EmptyOutputStream(final int chunkSize) throws Exception {
        final byte[] emptyEncoded = new byte[0];
        final byte[] emptyDecoded = new byte[0];
        testByteByByte(emptyEncoded, emptyDecoded, chunkSize, CR_LF);
        testByChunk(emptyEncoded, emptyDecoded, chunkSize, CR_LF);
    }
}
