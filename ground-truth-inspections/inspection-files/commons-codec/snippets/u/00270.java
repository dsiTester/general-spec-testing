public class Nysiis implements StringEncoder {

    /**
     * Retrieves the NYSIIS code for a given String object.
     *
     * @param str
     *            String to encode using the NYSIIS algorithm
     * @return A NYSIIS code for the String supplied
     */
    public String nysiis(String str) { // definition of b, reduced because lengthy
        ...
        final String string = key.toString();
        return this.isStrict() ? string.substring(0, Math.min(TRUE_LENGTH, string.length())) : string; // call to b
    }

    /**
     * Indicates the strict mode for this {@link Nysiis} encoder.
     *
     * @return {@code true} if the encoder is configured for strict mode, {@code false} otherwise
     */
    public boolean isStrict() { // definition of b
        return this.strict;
    }

    @Override
    public String encode(final String str) { // called from test
        return this.nysiis(str);             // call to a
    }

}

public class NysiisTest extends StringEncoderAbstractTest<Nysiis> {
    @Test
    public void testTrueVariant() {
        final Nysiis encoder = new Nysiis(true);

        final String encoded = encoder.encode("WESTERLUND");
        Assert.assertTrue(encoded.length() <= 6);
        Assert.assertEquals("WASTAR", encoded);
    }
}
