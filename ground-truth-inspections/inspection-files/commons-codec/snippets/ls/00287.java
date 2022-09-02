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
     * Gets if multiple phonetic encodings are concatenated or if just the first one is kept.
     *
     * @return true if multiple phonetic encodings are returned, false if just the first is
     */
    public boolean isConcat() { // definition of b
        return this.concat;
    }
}

public class BeiderMorseEncoder implements StringEncoder {
    public void setRuleType(final RuleType ruleType) {
        this.engine = new PhoneticEngine(this.engine.getNameType(), // call to a
                                         ruleType,
                                         this.engine.isConcat(), // call to b
                                         this.engine.getMaxPhonemes());
    }

}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testSetRuleTypeExact() {
        final BeiderMorseEncoder bmpm = new BeiderMorseEncoder();
        bmpm.setRuleType(RuleType.EXACT);
        assertEquals("Rule type should have been set to exact", RuleType.EXACT, bmpm.getRuleType());
    }
}
