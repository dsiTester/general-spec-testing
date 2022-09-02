public abstract class BaseNCodec {
    // package protected for access from I/O streams
    abstract void decode(byte[] pArray, int i, int length, Context context); // a

    @Override
    public byte[] decode(final byte[] pArray) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context);
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

        final byte[] buffer = ensureBufferSize(charsToProcess / BYTES_PER_ENCODED_BLOCK, context);

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

    private int decodeOctet(final byte octet) { // definition of b
        int decoded = -1;
        if ((octet & 0xff) < decodeTable.length) {
            decoded = decodeTable[octet];
        }

        if (decoded == -1) {
            throw new IllegalArgumentException("Invalid octet in encoded value: " + (int)octet);
        }

        return decoded;
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
