public class BeiderMorseEncoder implements StringEncoder {
    // a cached object
    private PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);

    /**
     * Sets the rule type to apply. This will widen or narrow the range of phonetic encodings considered.
     *
     * @param ruleType
     *            {@link RuleType#APPROX} or {@link RuleType#EXACT} for approximate or exact phonetic matches
     */
    public void setRuleType(final RuleType ruleType) { // definition of a
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         ruleType,
                                         this.engine.isConcat(),
                                         this.engine.getMaxPhonemes());
    }

    /**
     * Gets the rule type currently in operation.
     *
     * @return the RuleType currently being used
     */
    public RuleType getRuleType() { // definition of b
        return this.engine.getRuleType();
    }
}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testSetRuleTypeExact() {
        final BeiderMorseEncoder bmpm = new BeiderMorseEncoder();
        bmpm.setRuleType(RuleType.EXACT); // call to a
        assertEquals("Rule type should have been set to exact", RuleType.EXACT, bmpm.getRuleType()); // call to b
    }
}
