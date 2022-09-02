public class BeiderMorseEncoder implements StringEncoder {
    /**
     * Sets the number of maximum of phonemes that shall be considered by the engine.
     *
     * @param maxPhonemes
     *            the maximum number of phonemes returned by the engine
     * @since 1.7
     */
    public void setMaxPhonemes(final int maxPhonemes) { // definition of a
        this.engine = new PhoneticEngine(this.engine.getNameType(),
                                         this.engine.getRuleType(),
                                         this.engine.isConcat(),
                                         maxPhonemes);
    }

    @Override
    public String encode(final String source) throws EncoderException { // definition of b
        if (source == null) {
            return null;
        }
        return this.engine.encode(source);
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
        encoder.setRuleType(RuleType.EXACT);
        encoder.setMaxPhonemes(10); // call to a

        final String phonemes = encoder.encode(phrase); // call to b
        assertFalse(phonemes.isEmpty());

        final String[] phonemeArr = phonemes.split("\\|");
        assertTrue(phonemeArr.length <= 10);
    }
}
