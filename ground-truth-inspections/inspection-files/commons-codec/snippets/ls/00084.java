public class Base32 extends BaseNCodec {
    /**
     * Returns whether or not the {@code octet} is in the Base32 alphabet.
     *
     * @param octet
     *            The value to test
     * @return {@code true} if the value is defined in the the Base32 alphabet {@code false} otherwise.
     */
    @Override
    public boolean isInAlphabet(final byte octet) { // definition of a
        return octet >= 0 && octet < decodeTable.length && decodeTable[octet] != -1;
    }

    public Base32(final int lineLength, final byte[] lineSeparator, final boolean useHex,
                  final byte padding, final CodecPolicy decodingPolicy) { // calls a
        super(BYTES_PER_UNENCODED_BLOCK, BYTES_PER_ENCODED_BLOCK, lineLength,
                lineSeparator == null ? 0 : lineSeparator.length, padding, decodingPolicy);
        ...
        if (isInAlphabet(padding) || isWhiteSpace(padding)) { // call to a
            throw new IllegalArgumentException("pad must not be in alphabet or whitespace");
        }
    }
}

public abstract class BaseNCodec {
    /**
     * Encodes a byte[] containing binary data, into a byte[] containing characters in the alphabet.
     *
     * @param pArray
     *            a byte array containing binary data
     * @return A byte array containing only the base N alphabetic character data
     */
    @Override
    public byte[] encode(final byte[] pArray) { // definition of b
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        return encode(pArray, 0, pArray.length);
    }
}

public class BaseNTestData {
    static byte[][] randomData(final BaseNCodec codec, final int size) {
        final Random r = new Random();
        final byte[] decoded = new byte[size];
        r.nextBytes(decoded);
        final byte[] encoded = codec.encode(decoded); // call to b
        return new byte[][] {decoded, encoded};
    }
}

public class Base32Test {
    @Test
    public void testRandomBytes() {
        for (int i = 0; i < 20; i++) {
            final Base32 codec = new Base32(); // calls a
            final byte[][] b = BaseNTestData.randomData(codec, i); // calls b
            assertEquals(""+i+" "+codec.lineLength,b[1].length,codec.getEncodedLength(b[0]));
            //assertEquals(b[0],codec.decode(b[1]));
        }
    }
}
