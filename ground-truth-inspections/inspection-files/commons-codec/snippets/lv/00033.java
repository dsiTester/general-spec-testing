public class Base16 {

    /**
     * Ensure that the buffer has room for {@code size} bytes
     *
     * @param size minimum spare space required
     * @param context the context to be used
     * @return the buffer
     */
    protected byte[] ensureBufferSize(final int size, final Context context){ // definition of a
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

    private int decodeOctet(final byte octet) { // definition of b
        int decoded = -1;
        if ((octet & 0xff) < decodeTable.length) {
            decoded = decodeTable[octet];
        }

        if (decoded == -1) {
            throw new IllegalArgumentException("Invalid octet in encoded value: " + (int)octet); // throw exception here
        }

        return decoded;
    }

    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) {
        ...
        // NOTE: replace with null to experiment
        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // call to a
        ...

        while (i < charsToProcess) {
            result = decodeOctet(data[offset++]) << BITS_PER_ENCODED_BYTE; // call to b; IllegalArgumentException is thrown here
            // NOTE: call a below by uncommenting - however, we won't reach this part of the computation because the above throws an exception
            // System.out.println(ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context));
            result |= decodeOctet(data[offset++]);
            i += 2;
            buffer[context.pos++] = (byte)result;
        }

        ...
    }

}

public class BaseNCodec {
    @Override
    public byte[] decode(final byte[] pArray) { // called from test
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls a and b
        ...
        return result;
    }

}

public class Base16Test {
    /**
     * isBase16 throws RuntimeException on some
     * non-Base16 bytes
     */
    @Test(expected=RuntimeException.class) // expected exception
    public void testCodec68() {
        final byte[] x = new byte[] { 'n', 'H', '=', '=', (byte) 0x9c };
        final Base16 b16 = new Base16();
        b16.decode(x); // calls a and b
    }
}
