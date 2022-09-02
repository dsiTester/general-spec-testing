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
        return this.nysiis(str);             // call to b
    }

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
        return this.isStrict() ? string.substring(0, Math.min(TRUE_LENGTH, string.length())) : string;
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
