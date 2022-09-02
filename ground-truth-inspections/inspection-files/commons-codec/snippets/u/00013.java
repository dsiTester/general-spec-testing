public class BaseNCodec {
    @Override
    public byte[] decode(final byte[] pArray) { // definition of a; not defined in Base16
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        decode(pArray, 0, pArray.length, context); // calls b
        decode(pArray, 0, EOF, context); // Notify decoder of EOF.
        final byte[] result = new byte[context.pos];
        readResults(result, 0, result.length, context);
        return result;
    }
}

public class Base16 extends BaseNCodec {
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

    @Override
    void decode(final byte[] data, int offset, final int length, final Context context) { // called from a
        ...
        final int dataLen = Math.min(data.length - offset, length);
        final int availableChars = (context.ibitWorkArea != 0 ? 1 : 0) + dataLen;

        // small optimisation to short-cut the rest of this method when it is fed byte-by-byte
        if (availableChars == 1 && availableChars == dataLen) {
            // store 1/2 byte for next invocation of decode, we offset by +1 as empty-value is 0
            context.ibitWorkArea = decodeOctet(data[offset]) + 1; // call to b
            return;
        }
        ...
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
