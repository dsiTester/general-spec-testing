public class BeiderMorseEncoder implements StringEncoder {
    /**
     * Sets how multiple possible phonetic encodings are combined.
     *
     * @param concat
     *            true if multiple encodings are to be combined with a '|', false if just the first one is
     *            to be considered
     */
    public void setConcat(final boolean concat) { // definition of a
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         this.engine.getRuleType(),
                                         concat,
                                         this.engine.getMaxPhonemes());
    }

    /**
     * Discovers if multiple possible encodings are concatenated.
     *
     * @return true if multiple encodings are concatenated, false if just the first one is returned
     */
    public boolean isConcat() {
        return this.engine.isConcat();
    }
}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testSetConcat() {
        final BeiderMorseEncoder bmpm = new BeiderMorseEncoder();
        bmpm.setConcat(false);  // call to a
        assertFalse("Should be able to set concat to false", bmpm.isConcat()); // call to b
    }

}
