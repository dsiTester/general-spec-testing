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

    @Override
    public String encode(final String source) throws EncoderException { // definition of b
        if (source == null) {
            return null;
        }
        return this.engine.encode(source);
    }

}

public enum RuleType {          // reference for the value that method-a sets.

    /** Approximate rules, which will lead to the largest number of phonetic interpretations. */
    APPROX("approx"),
    /** Exact rules, which will lead to a minimum number of phonetic interpretations. */
    EXACT("exact"),
    /** For internal use only. Please use {@link #APPROX} or {@link #EXACT}. */
    RULES("rules");

    private final String name;

    RuleType(final String name) {
        this.name = name;
    }

    /**
     * Gets the rule name.
     *
     * @return the rule name.
     */
    public String getName() {
        return this.name;
    }

}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testOOM() throws EncoderException { // validating test
        final String phrase = "200697900'-->&#1913348150;</  bceaeef >aadaabcf\"aedfbff<!--\'-->?>cae" +
            "cfaaa><?&#<!--</script>&lang&fc;aadeaf?>>&bdquo<    cc =\"abff\"    /></   afe  >" +
            "<script><!-- f(';<    cf aefbeef = \"bfabadcf\" ebbfeedd = fccabeb >";

        final BeiderMorseEncoder encoder = new BeiderMorseEncoder();
        encoder.setNameType(NameType.GENERIC);
        encoder.setRuleType(RuleType.EXACT); // call to a
        encoder.setMaxPhonemes(10);

        final String phonemes = encoder.encode(phrase); // call to b
        assertFalse(phonemes.isEmpty());

        final String[] phonemeArr = phonemes.split("\\|");
        assertTrue(phonemeArr.length <= 10); // assertion fails here
    }

    @Test(timeout = 10000L)
    public void testLongestEnglishSurname() throws EncoderException { // invalidating test
        final BeiderMorseEncoder bmpm = createGenericApproxEncoder(); // calls a and b
        bmpm.encode("MacGhilleseatheanaich"); // call to b
    }

    private BeiderMorseEncoder createGenericApproxEncoder() {
        final BeiderMorseEncoder encoder = new BeiderMorseEncoder();
        encoder.setNameType(NameType.GENERIC);
        encoder.setRuleType(RuleType.APPROX); // call to a
        return encoder;
    }


}
