public class PhoneticEngine {
    /**
     * Encodes a string to its phonetic representation.
     *
     * @param input
     *            the String to encode
     * @return the encoding of the input
     */
    public String encode(final String input) { // definition of a
        final Languages.LanguageSet languageSet = this.lang.guessLanguages(input);
        return encode(input, languageSet); // call to b
    }

    /**
     * Encodes an input string into an output phonetic representation, given a set of possible origin languages.
     *
     * @param input
     *            String to phoneticise; a String with dashes or spaces separating each word
     * @param languageSet
     *            set of possible origin languages
     * @return a phonetic representation of the input; a String containing '-'-separated phonetic representations of the
     *         input
     */
    public String encode(String input, final Languages.LanguageSet languageSet) { // definition of b
        final Map<String, List<Rule>> rules = Rule.getInstanceMap(this.nameType, RuleType.RULES, languageSet);
        // rules common across many (all) languages
        final Map<String, List<Rule>> finalRules1 = Rule.getInstanceMap(this.nameType, this.ruleType, "common");
        // rules that apply to a specific language that may be ambiguous or wrong if applied to other languages
        final Map<String, List<Rule>> finalRules2 = Rule.getInstanceMap(this.nameType, this.ruleType, languageSet);

        // tidy the input
        // lower case is a locale-dependent operation
        input = input.toLowerCase(Locale.ENGLISH).replace('-', ' ').trim();

        if (this.nameType == NameType.GENERIC) {
            if (input.length() >= 2 && input.substring(0, 2).equals("d'")) { // check for d'
                final String remainder = input.substring(2);
                final String combined = "d" + remainder;
                return "(" + encode(remainder) + ")-(" + encode(combined) + ")";
            }
            for (final String l : NAME_PREFIXES.get(this.nameType)) {
                // handle generic prefixes
                if (input.startsWith(l + " ")) {
                    // check for any prefix in the words list
                    final String remainder = input.substring(l.length() + 1); // input without the prefix
                    final String combined = l + remainder; // input with prefix without space
                    return "(" + encode(remainder) + ")-(" + encode(combined) + ")";
                }
            }
        }

        final List<String> words = Arrays.asList(input.split("\\s+"));
        final List<String> words2 = new ArrayList<>();

        // special-case handling of word prefixes based upon the name type
        switch (this.nameType) {
        case SEPHARDIC:
            for (final String aWord : words) {
                final String[] parts = aWord.split("'");
                final String lastPart = parts[parts.length - 1];
                words2.add(lastPart);
            }
            words2.removeAll(NAME_PREFIXES.get(this.nameType));
            break;
        case ASHKENAZI:
            words2.addAll(words);
            words2.removeAll(NAME_PREFIXES.get(this.nameType));
            break;
        case GENERIC:
            words2.addAll(words);
            break;
        default:
            throw new IllegalStateException("Unreachable case: " + this.nameType);
        }

        if (this.concat) {
            // concat mode enabled
            input = join(words2, " ");
        } else if (words2.size() == 1) {
            // not a multi-word name
            input = words.iterator().next();
        } else {
            // encode each word in a multi-word name separately (normally used for approx matches)
            final StringBuilder result = new StringBuilder();
            for (final String word : words2) {
                result.append("-").append(encode(word));
            }
            // return the result without the leading "-"
            return result.substring(1);
        }

        PhonemeBuilder phonemeBuilder = PhonemeBuilder.empty(languageSet);

        // loop over each char in the input - we will handle the increment manually
        for (int i = 0; i < input.length();) {
            final RulesApplication rulesApplication =
                    new RulesApplication(rules, input, phonemeBuilder, i, maxPhonemes).invoke();
            i = rulesApplication.getI();
            phonemeBuilder = rulesApplication.getPhonemeBuilder();
        }

        // Apply the general rules
        phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules1);
        // Apply the language-specific rules
        phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules2);

        return phonemeBuilder.makeString();
    }
}

public class BeiderMorseEncoder implements StringEncoder {
    @Override
    public String encode(final String source) throws EncoderException {
        if (source == null) {
            return null;
        }
        return this.engine.encode(source); // call to a
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
        encoder.setMaxPhonemes(10);

        final String phonemes = encoder.encode(phrase); // calls a and b
        assertFalse(phonemes.isEmpty());

        final String[] phonemeArr = phonemes.split("\\|");
        assertTrue(phonemeArr.length <= 10);
    }
}
