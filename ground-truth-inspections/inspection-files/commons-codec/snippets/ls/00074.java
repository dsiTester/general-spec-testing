// both a and b not defined in Base32
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

    // package protected for access from I/O streams
    abstract void encode(byte[] pArray, int i, int length, Context context); // b
}

public class Base32 extends BaseNCodec {

    public Base32(final int lineLength, final byte[] lineSeparator, final boolean useHex,
                  final byte padding, final CodecPolicy decodingPolicy) { // indirectly called from constructor of Base32OutputStream
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
    void encode(final byte[] input, int inPos, final int inAvail, final Context context) { // definition of b, shortened
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
                ...
            }
            ...
        }
        ...
    }
}

public class Base32OutputStream extends BaseNCodecOutputStream {
    public Base32OutputStream(final OutputStream ouput, final boolean doEncode,
                              final int lineLength, final byte[] lineSeparator) {
        super(ouput, new Base32(lineLength, lineSeparator), doEncode); // calls a
    }
}

public class BaseNCodecOutputStream extends FilterOutputStream {
    @Override
    public void close() throws IOException { // called from BaseNCodecOutputStream.testByteByByte()
        eof();                  // calls b
        flush();
        out.close();
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
    public void testBase32EmptyOutputStreamMimeChunkSize() throws Exception {
        testBase32EmptyOutputStream(BaseNCodec.MIME_CHUNK_SIZE); // calls a and b
    }

    private void testBase32EmptyOutputStream(final int chunkSize) throws Exception {
        final byte[] emptyEncoded = new byte[0];
        final byte[] emptyDecoded = new byte[0];
        testByteByByte(emptyEncoded, emptyDecoded, chunkSize, CR_LF); // calls a and b
        testByChunk(emptyEncoded, emptyDecoded, chunkSize, CR_LF);
    }

    private void testByteByByte(final byte[] encoded, final byte[] decoded, final int chunkSize, final byte[] separator) throws Exception {

        // Start with encode.
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        OutputStream out = new Base32OutputStream(byteOut, true, chunkSize, separator); // calls a
        for (final byte element : decoded) {
            out.write(element);
        }
        out.close();            // calls b
        ...
        for (int i = 0; i < 10; i++) {
            out = new Base32OutputStream(out, false); // calls a
            out = new Base32OutputStream(out, true, chunkSize, separator); // calls a
        }
        for (final byte element : decoded) {
            out.write(element);
        }
        out.close();            // calls b
        ...
    }
}
