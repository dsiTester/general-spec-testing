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
        encoder.setNameType(NameType.GENERIC); // call to a
        encoder.setRuleType(RuleType.EXACT);
        encoder.setMaxPhonemes(10); // call to b

        final String phonemes = encoder.encode(phrase);
        assertFalse(phonemes.isEmpty());

        final String[] phonemeArr = phonemes.split("\\|");
        assertTrue(phonemeArr.length <= 10);
    }

}
