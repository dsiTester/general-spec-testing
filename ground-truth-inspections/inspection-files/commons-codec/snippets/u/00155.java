public abstract class BaseNCodec {
    // package protected for access from I/O streams
    abstract void encode(byte[] pArray, int i, int length, Context context); // a

    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of b
        return DEFAULT_BUFFER_SIZE;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){ // called from Base32.encode()
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
    void encode(final byte[] input, int inPos, final int inAvail, final Context context) { // used implementation of a
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
            final byte[] buffer = ensureBufferSize(encodeSize, context); // calls b
            final int savedPos = context.pos;
            ...
        }
        ...
    }
}

public class BaseNCodecOutputStream {
    public void eof() throws IOException {
        // Notify encoder of EOF (-1).
        if (doEncode) {
            baseNCodec.encode(singleByte, 0, EOF, context); // call toa
        } else {
            baseNCodec.decode(singleByte, 0, EOF, context);
        }
    }

}

public class Base32OutputStreamTest {
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
