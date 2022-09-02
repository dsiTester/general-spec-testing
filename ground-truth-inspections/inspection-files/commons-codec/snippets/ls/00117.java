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
    protected boolean containsAlphabetOrPad(final byte[] arrayOctet) { // definition of a; not defined in Base64
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

    /**
     * Returns the amount of buffered data available for reading.
     *
     * @param context the context to be used
     * @return The amount of buffered data available for reading.
     */
    int available(final Context context) {  // definition of b; not defined in Base64
        return context.buffer != null ? context.pos - context.readPos : 0;
    }

    @Override
    public byte[] decode(final byte[] pArray) { // called from BCodec
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        ...
        readResults(result, 0, result.length, context); // calls b
        return result;
    }

    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) {
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail); // call to b
            ...
            return len;
        }
        return context.eof ? EOF : 0;
    }

}

public class Base64 extends BaseNCodec {

    public Base64(final int lineLength, final byte[] lineSeparator, final boolean urlSafe,
                  final CodecPolicy decodingPolicy) {
        ...
        // @see test case Base64Test.testConstructors()
        if (lineSeparator != null) {
            if (containsAlphabetOrPad(lineSeparator)) { // call to a
                final String sep = StringUtils.newStringUtf8(lineSeparator);
                throw new IllegalArgumentException("lineSeparator must not contain base64 characters: [" + sep + "]");
            }
            ...
        } ...
    }

}

public class BCodec {
    @Override
    protected byte[] doDecoding(final byte[] bytes) { // eventually called from test
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes); // Base64() calls a, decode() calls b
    }

}

public class BCodecTest {
    @Test
    public void testBase64ImpossibleSamplesDefault() throws DecoderException {
        final BCodec codec = new BCodec();
        // Default encoding is lenient
        Assert.assertFalse(codec.isStrictDecoding()); // this is different from b
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            codec.decode(s); // calls a and b
        }
    }
}
