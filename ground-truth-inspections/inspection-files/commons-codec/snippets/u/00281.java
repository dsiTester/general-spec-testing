public class Rule {

    public static List<Rule> getInstance(final NameType nameType, final RuleType rt, final String lang) { // called from test
        return getInstance(nameType, rt, LanguageSet.from(new HashSet<>(Arrays.asList(lang)))); // calls a and b
    }

    public static List<Rule> getInstance(final NameType nameType, final RuleType rt,
                                         final Languages.LanguageSet langs) {
        final Map<String, List<Rule>> ruleMap = getInstanceMap(nameType, rt, langs); // calls a and b
        final List<Rule> allRules = new ArrayList<>();
        for (final List<Rule> rules : ruleMap.values()) {
            allRules.addAll(rules);
        }
        return allRules;
    }

    public static Map<String, List<Rule>> getInstanceMap(final NameType nameType, final RuleType rt,
                                                         final Languages.LanguageSet langs) {
        return langs.isSingleton() ? getInstanceMap(nameType, rt, langs.getAny()) : // call to a and b; getInstanceMap() here throws an IllegalArgumentException.
                                     getInstanceMap(nameType, rt, Languages.ANY);
    }

}

public class Languages {
    public static abstract class LanguageSet {
        ...
        public abstract boolean isSingleton(); // a
        ...
        public abstract String getAny(); // b
    }

    public static final class SomeLanguages extends LanguageSet {
        private final Set<String> languages;
        ...
        @Override
        public boolean isSingleton() { // used implementation of a
            return this.languages.size() == 1;
        }
        ...
        @Override
        public String getAny() { // used implementation of b
            return this.languages.iterator().next();
        }
    }
}

public class BeiderMorseEncoderTest extends StringEncoderAbstractTest<StringEncoder> {
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLangIllegalArgumentException() {
        Rule.getInstance(NameType.GENERIC, RuleType.APPROX, "noSuchLanguage");
    }
}
