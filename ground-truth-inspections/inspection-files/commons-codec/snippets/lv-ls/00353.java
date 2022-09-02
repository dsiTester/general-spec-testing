public class PercentCodec {
    /**
     * Inserts a single character into a BitSet and maintains the min and max of the characters of the
     * {@code BitSet alwaysEncodeChars} in order to avoid look-ups when a byte is out of this range.
     *
     * @param b the byte that is candidate for min and max limit
     */
    private void insertAlwaysEncodeChar(final byte b) { // definition of a
        this.alwaysEncodeChars.set(b);
        if (b < alwaysEncodeCharsMin) {
            alwaysEncodeCharsMin = b;
        }
        if (b > alwaysEncodeCharsMax) {
            alwaysEncodeCharsMax = b;
        }
    }

    /**
     * Encodes an object into using the Percent-Encoding. Only byte[] objects are accepted.
     *
     * @param obj the object to encode
     * @return the encoding result byte[] as Object
     * @throws EncoderException if the object is not a byte array
     */
    @Override
    public Object encode(final Object obj) throws EncoderException { // definition of b
        if (obj == null) {
            return null;
        }
        if (obj instanceof byte[]) {
            return encode((byte[]) obj);
        }
        throw new EncoderException("Objects of type " + obj.getClass().getName() + " cannot be Percent encoded");
    }

    public PercentCodec() {     // called from validated test
        this.plusForSpace = false;
        insertAlwaysEncodeChar(ESCAPE_CHAR); // call to a
    }

    public PercentCodec(final byte[] alwaysEncodeChars, final boolean plusForSpace) { // called from invalidated test
        this.plusForSpace = plusForSpace;
        insertAlwaysEncodeChars(alwaysEncodeChars); // calls a
    }

    private void insertAlwaysEncodeChars(final byte[] alwaysEncodeCharsArray) { // called from above
        if (alwaysEncodeCharsArray != null) {
            for (final byte b : alwaysEncodeCharsArray) {
                insertAlwaysEncodeChar(b); // this also seems like a call to a, but most likely wasn't called from this test
            }
        }
        insertAlwaysEncodeChar(ESCAPE_CHAR); // call to a
    }
}

public class PercentCodecTest {

    @Test(expected = EncoderException.class)
    public void testEncodeUnsupportedObject() throws Exception { // validated test
        final PercentCodec percentCodec = new PercentCodec();    // calls a
        percentCodec.encode("test"); // call to b
    }

    @Test
    public void testSafeCharEncodeDecodeObject() throws Exception { // invalidated test
        final PercentCodec percentCodec = new PercentCodec(null, true); // calls a
        final String input = "abc123_-.*";
        final Object encoded = percentCodec.encode((Object) input.getBytes(StandardCharsets.UTF_8)); // call to b
        final String encodedS = new String((byte[]) encoded, "UTF-8");
        final Object decoded = percentCodec.decode(encoded);
        final String decodedS = new String((byte[]) decoded, "UTF-8");
        assertEquals("Basic PercentCodec safe char encoding test", input, encodedS);
        assertEquals("Basic PercentCodec safe char decoding test", input, decodedS);
    }

}
