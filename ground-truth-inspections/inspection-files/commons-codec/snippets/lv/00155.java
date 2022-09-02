public abstract class BaseNCodec {
    /**
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of a
        return DEFAULT_BUFFER_SIZE;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){ // called from Base16.decode()
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to a
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

public class Base16 extends BaseNCodec {
    private int decodeOctet(final byte octet) { // definition of b
        int decoded = -1;
        if ((octet & 0xff) < decodeTable.length) {
            decoded = decodeTable[octet];
        }

        if (decoded == -1) {
            throw new IllegalArgumentException("Invalid octet in encoded value: " + (int)octet); // expected exception thrown here
        }

        return decoded;
    }

    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // transitively called from test
        ...
        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // calls a
        ...
        while (i < charsToProcess) {
            result = decodeOctet(data[offset++]) << BITS_PER_ENCODED_BYTE; // call to b
            result |= decodeOctet(data[offset++]);
            i += 2;
            buffer[context.pos++] = (byte)result;
        }

        // we have one char of a hex-pair left over
        if (i < dataLen) {
            // store 1/2 byte for next invocation of decode, we offset by +1 as empty-value is 0
            context.ibitWorkArea = decodeOctet(data[i]) + 1;
        }
    }
}

public class Base16Test {
    /**
     * isBase16 throws RuntimeException on some
     * non-Base16 bytes
     */
    @Test(expected=RuntimeException.class)
    public void testCodec68() {
        final byte[] x = new byte[] { 'n', 'H', '=', '=', (byte) 0x9c };
        final Base16 b16 = new Base16();
        b16.decode(x);
    }
}
