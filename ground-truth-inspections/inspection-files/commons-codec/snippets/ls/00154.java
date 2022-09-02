public class Base16 extends BaseNCodec {
    @Override
    void encode(final byte[] data, final int offset, final int length, final Context context) { // used implementation of a
        if (context.eof) {
            return;
        }

        if (length < 0) {
            context.eof = true;
            return;
        }

        final int size = length * BYTES_PER_ENCODED_BLOCK;
        if (size < 0) {
            throw new IllegalArgumentException("Input length exceeds maximum size for encoded data: " + length);
        }

        final byte[] buffer = ensureBufferSize(size, context); // modifies context.buffer

        final int end = offset + length;
        for (int i = offset; i < end; i++) {
            final int value = data[i];
            final int high = (value >> BITS_PER_ENCODED_BYTE) & MASK_4BITS;
            final int low = value & MASK_4BITS;
            buffer[context.pos++] = encodeTable[high];
            buffer[context.pos++] = encodeTable[low];
        }
    }

}

public abstract class BaseNCodec {
    // package protected for access from I/O streams
    abstract void encode(byte[] pArray, int i, int length, Context context); // a

    /**
     * Returns the amount of buffered data available for reading.
     *
     * @param context the context to be used
     * @return The amount of buffered data available for reading.
     */
    int available(final Context context) {  // definition of b
        return context.buffer != null ? context.pos - context.readPos : 0;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){ // called from a
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())];
            context.pos = 0;
            context.readPos = 0;

            // Overflow-conscious:
            // x + y > z  ==  x + y - z > 0
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }
}

public class BaseNCodecOutputStream {
    public void eof() throws IOException {
        // Notify encoder of EOF (-1).
        if (doEncode) {
            baseNCodec.encode(singleByte, 0, EOF, context); // call to a
        } else {
            baseNCodec.decode(singleByte, 0, EOF, context);
        }
    }

    private void flush(final boolean propagate) throws IOException {
        final int avail = baseNCodec.available(context); // call to b
        if (avail > 0) {
            final byte[] buf = new byte[avail];
            final int c = baseNCodec.readResults(buf, 0, avail, context);
            if (c > 0) {
                out.write(buf, 0, c);
            }
        }
        if (propagate) {
            out.flush();
        }
    }

}

public class Base16OutputStreamTest {
    /**
     * Tests Base16OutputStream.write for expected IndexOutOfBoundsException conditions.
     *
     * @throws IOException for some failure scenarios.
     */
    @Test
    public void testWriteOutOfBounds() throws IOException {
        final byte[] buf = new byte[1024];
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (final Base16OutputStream out = new Base16OutputStream(bout)) {

            try {
                out.write(buf, -1, 1);
                fail("Expected Base16OutputStream.write(buf, -1, 1) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, 1, -1);
                fail("Expected Base16OutputStream.write(buf, 1, -1) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, buf.length + 1, 0);
                fail("Expected Base16OutputStream.write(buf, buf.length + 1, 0) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }

            try {
                out.write(buf, buf.length - 1, 2);
                fail("Expected Base16OutputStream.write(buf, buf.length - 1, 2) to throw a IndexOutOfBoundsException");
            } catch (final IndexOutOfBoundsException ioobe) {
                // Expected
            }
        }
    }
}
