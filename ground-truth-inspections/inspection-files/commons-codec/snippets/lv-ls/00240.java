public class MatchRatingApproachEncoder {
    /**
     * Cleans up a name: 1. Upper-cases everything 2. Removes some common punctuation 3. Removes accents 4. Removes any
     * spaces.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            The name to be cleaned
     * @return The cleaned name
     */
    String cleanName(final String name) { // definition of a
        String upperName = name.toUpperCase(Locale.ENGLISH);

        final String[] charsToTrim = { "\\-", "[&]", "\\'", "\\.", "[\\,]" };
        for (final String str : charsToTrim) {
            upperName = upperName.replaceAll(str, EMPTY);
        }

        upperName = removeAccents(upperName);
        upperName = upperName.replaceAll("\\s+", EMPTY);

        return upperName;
    }

    /**
     * Replaces any double consonant pair with the single letter equivalent.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            String to have double consonants removed
     * @return Single consonant word
     */
    String removeDoubleConsonants(final String name) { // definition of b
        String replacedName = name.toUpperCase(Locale.ENGLISH);
        for (final String dc : DOUBLE_CONSONANT) {
            if (replacedName.contains(dc)) {
                final String singleLetter = dc.substring(0, 1);
                replacedName = replacedName.replace(dc, singleLetter);
            }
        }
        return replacedName;
    }

    @Override
    public final String encode(String name) { // called from tests
        // Bulletproof for trivial input - NINO
        if (name == null || EMPTY.equalsIgnoreCase(name) || SPACE.equalsIgnoreCase(name) || name.length() == 1) {
            return EMPTY;
        }

        // Preprocessing
        name = cleanName(name); // call to a

        // BEGIN: Actual encoding part of the algorithm...
        // 1. Delete all vowels unless the vowel begins the word
        name = removeVowels(name);

        // 2. Remove second consonant from any double consonant
        name = removeDoubleConsonants(name); // call to b

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name);

        return name;
    }
}

public class MatchRatingApproachEncoderTest {
    @Test
    public final void testGetEncoding_SMITH_to_SMTH() { // validated test
        assertEquals("SMTH", this.getStringEncoder().encode("Smith")); // calls a and b
    }

    @Test
    public final void testGetEncoding_HARPER_HRPR() { // invalidated test
        assertEquals("HRPR", this.getStringEncoder().encode("HARPER"));
    }

}
