public class PhoneticEngine {
    private static final class RulesApplication {
        /**
         * Invokes the rules. Loops over the rules list, stopping at the first one that has a matching context
         * and pattern. Then applies this rule to the phoneme builder to produce updated phonemes. If there was no
         * match, {@code i} is advanced one and the character is silently dropped from the phonetic spelling.
         *
         * @return {@code this}
         */
        public RulesApplication invoke() { // definition of a
            this.found = false;
            int patternLength = 1;
            final List<Rule> rules = this.finalRules.get(input.subSequence(i, i+patternLength));
            if (rules != null) {
                for (final Rule rule : rules) {
                    final String pattern = rule.getPattern();
                    patternLength = pattern.length();
                    if (rule.patternAndContextMatches(this.input, this.i)) {
                        this.phonemeBuilder.apply(rule.getPhoneme(), maxPhonemes);
                        this.found = true;
                        break;
                    }
                }
            }

            if (!this.found) {
                patternLength = 1;
            }

            this.i += patternLength;
            return this;
        }

        public int getI() {     // definition of b
            return this.i;
        }

    }
    public String encode(String input, final Languages.LanguageSet languageSet) {
        final Map<String, List<Rule>> rules = Rule.getInstanceMap(this.nameType, RuleType.RULES, languageSet);
        // rules common across many (all) languages
        final Map<String, List<Rule>> finalRules1 = Rule.getInstanceMap(this.nameType, this.ruleType, "common");
        // rules that apply to a specific language that may be ambiguous or wrong if applied to other languages
        final Map<String, List<Rule>> finalRules2 = Rule.getInstanceMap(this.nameType, this.ruleType, languageSet);

        ...
        PhonemeBuilder phonemeBuilder = PhonemeBuilder.empty(languageSet);

        // loop over each char in the input - we will handle the increment manually
        for (int i = 0; i < input.length();) {
            final RulesApplication rulesApplication =
                    new RulesApplication(rules, input, phonemeBuilder, i, maxPhonemes).invoke(); // call to a
            i = rulesApplication.getI(); // call to b
            phonemeBuilder = rulesApplication.getPhonemeBuilder(); // NullPointerException here
        }

        // Apply the general rules
        phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules1); // calls a and b? below
        // Apply the language-specific rules
        phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules2);

        return phonemeBuilder.makeString();
    }

    private PhonemeBuilder applyFinalRules(final PhonemeBuilder phonemeBuilder,
                                           final Map<String, List<Rule>> finalRules) {
        ...
        for (final Rule.Phoneme phoneme : phonemeBuilder.getPhonemes()) {
            PhonemeBuilder subBuilder = PhonemeBuilder.empty(phoneme.getLanguages());
            final String phonemeText = phoneme.getPhonemeText().toString();

            for (int i = 0; i < phonemeText.length();) {
                final RulesApplication rulesApplication =
                        new RulesApplication(finalRules, phonemeText, subBuilder, i, maxPhonemes).invoke(); // call to a
                final boolean found = rulesApplication.isFound();
                subBuilder = rulesApplication.getPhonemeBuilder();

                if (!found) {
                    // not found, appending as-is
                    subBuilder.append(phonemeText.subSequence(i, i + 1));
                }

                i = rulesApplication.getI(); // call to b
            }

            ...

        return new PhonemeBuilder(phonemes.keySet());
    }


}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test
    public void testAsciiEncodeNotEmpty1Letter() throws EncoderException { // unknown verdict test
        final BeiderMorseEncoder bmpm = createGenericApproxEncoder();
        for (char c = 'a'; c <= 'z'; c++) {
            final String value = Character.toString(c);
            final String valueU = value.toUpperCase();
            assertNotEmpty(bmpm, value);
            assertNotEmpty(bmpm, valueU);
        }
    }

    /**
     * Tests we do not blow up.
     *
     * @throws EncoderException for some failure scenarios     */
    @Test
    public void testAllChars() throws EncoderException { // error verdict test
        final BeiderMorseEncoder bmpm = createGenericApproxEncoder();
        for (char c = Character.MIN_VALUE; c < Character.MAX_VALUE; c++) {
            bmpm.encode(Character.toString(c));
        }
    }
}
