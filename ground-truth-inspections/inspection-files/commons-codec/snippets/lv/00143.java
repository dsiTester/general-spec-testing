public abstract class BaseNCodec {
    // package protected for access from I/O streams
    abstract void decode(byte[] pArray, int i, int length, Context context); // a

    /**
     * Ensure that the buffer has room for {@code size} bytes
     *
     * @param size minimum spare space required
     * @param context the context to be used
     * @return the buffer
     */
    protected byte[] ensureBufferSize(final int size, final Context context){ // definition of b
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

    @Override
    public byte[] decode(final byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // call to a
        decode(pArray, 0, EOF, context); // Notify decoder of EOF.
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }
}

public class Base16 {
    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // used implementation of a
        if (context.eof || length < 0) {
            context.eof = true;
            if (context.ibitWorkArea != 0) {
                validateTrailingCharacter();
            }
            return;
        }

        final int dataLen = Math.min(data.length - offset, length);
        final int availableChars = (context.ibitWorkArea != 0 ? 1 : 0) + dataLen;

        // small optimisation to short-cut the rest of this method when it is fed byte-by-byte
        if (availableChars == 1 && availableChars == dataLen) {
            // store 1/2 byte for next invocation of decode, we offset by +1 as empty-value is 0
            context.ibitWorkArea = decodeOctet(data[offset]) + 1;
            return;
        }

        // we must have an even number of chars to decode
        final int charsToProcess = availableChars % BYTES_PER_ENCODED_BLOCK == 0 ? availableChars : availableChars - 1;

        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context); // call to b

        int result;
        int i = 0;
        if (dataLen < availableChars) {
            // we have 1/2 byte from previous invocation to decode
            result = (context.ibitWorkArea - 1) << BITS_PER_ENCODED_BYTE;
            result |= decodeOctet(data[offset++]);
            i = 2;

            buffer[context.pos++] = (byte)result;

            // reset to empty-value for next invocation!
            context.ibitWorkArea = 0;
        }

        while (i < charsToProcess) {
            result = decodeOctet(data[offset++]) << BITS_PER_ENCODED_BYTE;
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
