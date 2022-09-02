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
     * Extracts buffered data into the provided byte[] array, starting at position bPos, up to a maximum of bAvail
     * bytes. Returns how many bytes were actually extracted.
     * <p>
     * Package protected for access from I/O streams.
     *
     * @param b
     *            byte[] array to extract the buffered data into.
     * @param bPos
     *            position in byte[] array to start extraction at.
     * @param bAvail
     *            amount of bytes we're allowed to extract. We may extract fewer (if fewer are available).
     * @param context
     *            the context to be used
     * @return The number of bytes successfully extracted into the provided byte[] array.
     */
    int readResults(final byte[] b, final int bPos, final int bAvail, final Context context) { // definition of b
        if (context.buffer != null) {
            final int len = Math.min(available(context), bAvail);
            System.arraycopy(context.buffer, context.readPos, b, bPos, len);
            context.readPos += len;
            if (context.readPos >= context.pos) {
                context.buffer = null; // so hasData() will return false, and this method can return -1
            }
            return len;
        }
        return context.eof ? EOF : 0;
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
        ...
        readResults(buf, 0, buf.length, context); // call to b
        return buf;
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
