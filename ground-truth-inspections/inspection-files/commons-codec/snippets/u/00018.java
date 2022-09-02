public class BaseNCodec {
    /**
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    @Override
    public byte[] decode(final byte[] pArray) { // definition of a; a is not defined in Base16
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // call to b
        decode(pArray, 0, EOF, context); // Notify decoder of EOF.
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }

    // package protected for access from I/O streams
    abstract void decode(byte[] pArray, int i, int length, Context context); // b; implemented in Base16
}

public class Base16 extends BaseNCodec {
    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // used implementation of b
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
    @Test
    public void testNonBase16Test() {
        final byte[] invalidEncodedChars = { '/', ':', '@', 'G', '%', '`', 'g' };

        final byte[] encoded = new byte[1];
        for (final byte invalidEncodedChar : invalidEncodedChars) {
            try {
                encoded[0] = invalidEncodedChar;
                new Base16().decode(encoded); // call to a
                fail("IllegalArgumentException should have been thrown when trying to decode invalid Base16 char: " + (char)invalidEncodedChar);
            } catch (final Exception e) {
                assertTrue(e instanceof IllegalArgumentException);
            }
        }
    }
}
