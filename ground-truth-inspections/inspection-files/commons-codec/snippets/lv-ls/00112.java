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
    protected boolean containsAlphabetOrPad(final byte[] arrayOctet) { // definition of a
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
     * Decodes a byte[] containing characters in the Base-N alphabet.
     *
     * @param pArray
     *            A byte array containing Base-N character data
     * @return a byte array containing binary data
     */
    @Override
    public byte[] decode(final byte[] pArray) { // definition of b
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

public class Base64 extends BaseNCodec {
    public Base64(final int lineLength, final byte[] lineSeparator, final boolean urlSafe,
                  final CodecPolicy decodingPolicy) {
        ...
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
    public Object decode(final Object value) throws DecoderException { // called from test
        ...
        if (value instanceof String) {
            return decode((String) value); // calls a and b
        }
        ...
    }

    @Override
    public String decode(final String value) throws DecoderException {
        if (value == null) {
            return null;
        }
        try {
            return this.decodeText(value); // calls a and b
        } catch (final UnsupportedEncodingException | IllegalArgumentException e) {
            throw new DecoderException(e.getMessage(), e);
        }
    }

    @Override
    protected byte[] doDecoding(final byte[] bytes) { // called from RFC1522Codec
        if (bytes == null) {
            return null;
        }
        return new Base64(0, BaseNCodec.getChunkSeparator(), false, decodingPolicy).decode(bytes); // Base64() calls a; decode() is call to b
    }

}

public class RFC1522Codec {
    protected String decodeText(final String text)
        throws DecoderException, UnsupportedEncodingException { // called from BCodec.decode(String)
        ...
        data = doDecoding(data); // calls a and b
        return new String(data, charset);
    }
}

public class BCodecTest {
    @Test
    public void testBase64ImpossibleSamplesStrict() throws DecoderException { // validated test
        final BCodec codec = new BCodec(StandardCharsets.UTF_8, CodecPolicy.STRICT);
        Assert.assertTrue(codec.isStrictDecoding());
        for (final String s : BASE64_IMPOSSIBLE_CASES) {
            try {
                codec.decode(s); // calls a and b
                fail("Expected an exception for impossible case");
            } catch (final DecoderException ex) {
                // expected
            }
        }
    }

    @Test
    public void testDecodeObjects() throws Exception { // invalidated test
        final BCodec bcodec = new BCodec();
        final String decoded = "=?UTF-8?B?d2hhdCBub3Q=?=";
        final String plain = (String) bcodec.decode((Object) decoded); // calls a and b
        assertEquals("Basic B decoding test", "what not", plain);

        final Object result = bcodec.decode((Object) null);
        assertEquals("Decoding a null Object should return null", null, result);

        try {
            final Object dObj = Double.valueOf(3.0d);
            bcodec.decode(dObj);
            fail("Trying to url encode a Double object should cause an exception.");
        } catch (final DecoderException ee) {
            // Exception expected, test segment passes.
        }
    }
}
