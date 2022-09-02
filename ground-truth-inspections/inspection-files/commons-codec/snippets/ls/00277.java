public class BeiderMorseEncoder implements StringEncoder {

    /**
     * Sets the type of name. Use {@link NameType#GENERIC} unless you specifically want phonetic encodings
     * optimized for Ashkenazi or Sephardic Jewish family names.
     *
     * @param nameType
     *            the NameType in use
     */
    public void setNameType(final NameType nameType) { // definition of a
        this.engine = new PhoneticEngine(nameType,
                                         this.engine.getRuleType(),
                                         this.engine.isConcat(),
                                         this.engine.getMaxPhonemes());
    }

    /**
     * Sets the rule type to apply. This will widen or narrow the range of phonetic encodings considered.
     *
     * @param ruleType
     *            {@link RuleType#APPROX} or {@link RuleType#EXACT} for approximate or exact phonetic matches
     */
    public void setRuleType(final RuleType ruleType) { // definition of b
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         ruleType,
                                         this.engine.isConcat(),
                                         this.engine.getMaxPhonemes());
    }


}


public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testOOM() throws EncoderException {
        final String phrase = "200697900'-->&#1913348150;</  bceaeef >aadaabcf\"aedfbff<!--\'-->?>cae" +
            "cfaaa><?&#<!--</script>&lang&fc;aadeaf?>>&bdquo<    cc =\"abff\"    /></   afe  >" +
            "<script><!-- f(';<    cf aefbeef = \"bfabadcf\" ebbfeedd = fccabeb >";

        final BeiderMorseEncoder encoder = new BeiderMorseEncoder();
        encoder.setNameType(NameType.GENERIC); // call to a
        encoder.setRuleType(RuleType.EXACT); // call to b
        encoder.setMaxPhonemes(10);

        final String phonemes = encoder.encode(phrase);
        assertFalse(phonemes.isEmpty());

        final String[] phonemeArr = phonemes.split("\\|");
        assertTrue(phonemeArr.length <= 10);
    }

}
