public class Nysiis implements StringEncoder {
    /**
     * Encodes a String using the NYSIIS algorithm.
     *
     * @param str
     *            A String object to encode
     * @return A Nysiis code corresponding to the String supplied
     * @throws IllegalArgumentException
     *            if a character is not mapped
     */
    @Override
    public String encode(final String str) { // definition of a
        return this.nysiis(str);
    }

    /**
     * Indicates the strict mode for this {@link Nysiis} encoder.
     *
     * @return {@code true} if the encoder is configured for strict mode, {@code false} otherwise
     */
    public boolean isStrict() { // definition of b
        return this.strict;
    }

    public String nysiis(String str) { // called from a
        ...
        final String string = key.toString();
        return this.isStrict() ? string.substring(0, Math.min(TRUE_LENGTH, string.length())) : string; // call to b
    }

}

public class NysiisTest extends StringEncoderAbstractTest<Nysiis> {
    @Test
    public void testTrueVariant() {
        final Nysiis encoder = new Nysiis(true);

        final String encoded = encoder.encode("WESTERLUND"); // call to a
        Assert.assertTrue(encoded.length() <= 6);
        Assert.assertEquals("WASTAR", encoded);
    }
}
