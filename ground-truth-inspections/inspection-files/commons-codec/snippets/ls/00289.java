public class PhoneticEngine {
    /**
     * Gets the RuleType being used.
     *
     * @return the RuleType in use
     */
    public RuleType getRuleType() { // definition of a
        return this.ruleType;
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
    public void setNameType(final NameType nameType) {
        this.engine = new PhoneticEngine(nameType,
                                         this.engine.getRuleType(), // call to a
                                         this.engine.isConcat(), // call to b
                                         this.engine.getMaxPhonemes());
    }


}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testSetNameTypeAsh() {
        final BeiderMorseEncoder bmpm = new BeiderMorseEncoder();
        bmpm.setNameType(NameType.ASHKENAZI); // calls a and b
        assertEquals("Name type should have been set to ash", NameType.ASHKENAZI, bmpm.getNameType());
    }
}
