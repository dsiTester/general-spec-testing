public class PhoneticEngine {
    /**
     * Gets the NameType being used.
     *
     * @return the NameType in use
     */
    public NameType getNameType() { // definition of a
        return this.nameType;
    }

    /**
     * Gets the maximum number of phonemes the engine will calculate for a given input.
     *
     * @return the maximum number of phonemes
     * @since 1.7
     */
    public int getMaxPhonemes() { // definition of b
        return this.maxPhonemes;
    }
}

public class BeiderMorseEncoder implements StringEncoder {
    public void setConcat(final boolean concat) {
        this.engine = new PhoneticEngine(this.engine.getNameType(), // call to a
                                         this.engine.getRuleType(),
                                         concat,
                                         this.engine.getMaxPhonemes()); // call to b
    }

}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testSetConcat() {
        final BeiderMorseEncoder bmpm = new BeiderMorseEncoder();
        bmpm.setConcat(false);  // calls a and b
        assertFalse("Should be able to set concat to false", bmpm.isConcat());
    }
}
