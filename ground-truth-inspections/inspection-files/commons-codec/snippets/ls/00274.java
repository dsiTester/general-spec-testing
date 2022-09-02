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

    @Override
    public String encode(final String source) throws EncoderException { // definition of b
        if (source == null) {
            return null;
        }
        return this.engine.encode(source);
    }
}


public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    /**
     * Tests https://issues.apache.org/jira/browse/CODEC-125?focusedCommentId=13071566&page=com.atlassian.jira.plugin.system.issuetabpanels:
     * comment-tabpanel#comment-13071566
     *
     * @throws EncoderException for some failure scenarios     */
    @Test
    public void testEncodeGna() throws EncoderException {
        final BeiderMorseEncoder bmpm = createGenericApproxEncoder(); // calls a
        bmpm.encode("gna");     // call to b
    }

    private BeiderMorseEncoder createGenericApproxEncoder() {
        final BeiderMorseEncoder encoder = new BeiderMorseEncoder();
        encoder.setNameType(NameType.GENERIC); // call to a
        encoder.setRuleType(RuleType.APPROX);
        return encoder;
    }

}
