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
     * Gets the RuleType being used.
     *
     * @return the RuleType in use
     */
    public RuleType getRuleType() { // definition of b
        return this.ruleType;
    }
}

public class BeiderMorseEncoder implements StringEncoder {
    public void setConcat(final boolean concat) {
        this.engine = new PhoneticEngine(this.engine.getNameType(), // call to a
                                         this.engine.getRuleType(), // call to b
                                         concat,
                                         this.engine.getMaxPhonemes());
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
