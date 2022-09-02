public class BeiderMorseEncoder implements StringEncoder {

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
     * Sets the number of maximum of phonemes that shall be considered by the engine.
     *
     * @param maxPhonemes
     *            the maximum number of phonemes returned by the engine
     * @since 1.7
     */
    public void setMaxPhonemes(final int maxPhonemes) { // definition of b
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         this.engine.getRuleType(),
                                         this.engine.isConcat(),
                                         maxPhonemes);
    }

}


public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testOOM() throws EncoderException {
        final String phrase = "200697900'-->&#1913348150;</  bceaeef >aadaabcf\"aedfbff<!--\'-->?>cae" +
            "cfaaa><?&#<!--</script>&lang&fc;aadeaf?>>&bdquo<    cc =\"abff\"    /></   afe  >" +
            "<script><!-- f(';<    cf aefbeef = \"bfabadcf\" ebbfeedd = fccabeb >";

        final BeiderMorseEncoder encoder = new BeiderMorseEncoder();
        encoder.setNameType(NameType.GENERIC);
        encoder.setRuleType(RuleType.EXACT); // call to a
        encoder.setMaxPhonemes(10); // call to b

        final String phonemes = encoder.encode(phrase);
        assertFalse(phonemes.isEmpty());

        final String[] phonemeArr = phonemes.split("\\|");
        assertTrue(phonemeArr.length <= 10);
    }

}
