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
     * Get the default buffer size. Can be overridden.
     *
     * @return the default buffer size.
     */
    protected int getDefaultBufferSize() { // definition of b
        return DEFAULT_BUFFER_SIZE;
    }

    @Override
    public byte[] encode(final byte[] pArray) { // called from Base64.encodeBase64()
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        return encode(pArray, 0, pArray.length);
    }

    public byte[] encode(final byte[] pArray, final int offset, final int length) {
        if (pArray == null || pArray.length == 0) {
            return pArray;
        }
        final Context context = new Context();
        encode(pArray, offset, length, context); // calls b
        ...
        return buf;
    }

    protected byte[] ensureBufferSize(final int size, final Context context){ // called from Base64.encode()
        if (context.buffer == null) {
            context.buffer = new byte[Math.max(size, getDefaultBufferSize())]; // call to b
            ...
        } else if (context.pos + size - context.buffer.length > 0) {
            return resizeBuffer(context, context.pos + size);
        }
        return context.buffer;
    }
}

public class Base64 extends BaseNCodec {
    public static byte[] encodeBase64(final byte[] binaryData, final boolean isChunked,
                                      final boolean urlSafe, final int maxResultSize) { // called from wrappers called from test
        ...
        final Base64 b64 = isChunked ? new Base64(urlSafe) : new Base64(0, CHUNK_SEPARATOR, urlSafe); // calls a
        final long len = b64.getEncodedLength(binaryData);
        if (len > maxResultSize) {
            throw new IllegalArgumentException("Input array too big, the output array would be bigger (" +
                                               len +
                                               ") than the specified maximum size of " +
                                               maxResultSize);
        }

        return b64.encode(binaryData); // calls b
    }

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

    @Override
    void encode(final byte[] in, int inPos, final int inAvail, final Context context) { // called from BaseNCodec.encode()
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
            ...
        }
        ...
    }
}


public class Base64Codec13Test {
    @Test
    public void testStaticEncode() throws EncoderException {
        for (int i = 0; i < STRINGS.length; i++) {
            if (STRINGS[i] != null) {
                final byte[] base64 = utf8(STRINGS[i]);
                final byte[] binary = BYTES[i];
                final boolean b = Arrays.equals(base64, Base64.encodeBase64(binary));
                assertTrue("static Base64.encodeBase64() test-" + i, b);
            }
        }
    }

}
