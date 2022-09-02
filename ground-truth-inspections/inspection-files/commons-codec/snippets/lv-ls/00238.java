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
     * Determines if a letter is a vowel.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param letter
     *            The letter under investiagtion
     * @return True if a vowel, else false
     */
    boolean isVowel(final String letter) {
        return letter.equalsIgnoreCase("E") || letter.equalsIgnoreCase("A") || letter.equalsIgnoreCase("O") ||
               letter.equalsIgnoreCase("I") || letter.equalsIgnoreCase("U");
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
        name = removeVowels(name); // calls b

        // 2. Remove second consonant from any double consonant
        name = removeDoubleConsonants(name);

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name);

        return name;
    }

    String removeVowels(String name) { // called from encode()
        // Extract first letter
        final String firstLetter = name.substring(0, 1);

        name = name.replace("A", EMPTY);
        name = name.replace("E", EMPTY);
        name = name.replace("I", EMPTY);
        name = name.replace("O", EMPTY);
        name = name.replace("U", EMPTY);

        name = name.replaceAll("\\s{2,}\\b", SPACE);

        // return isVowel(firstLetter) ? (firstLetter + name) : name;
        if (isVowel(firstLetter)) { // call to b
            return firstLetter + name;
        }
        return name;
    }
}

public class MatchRatingApproachEncoderTest {
    @Test
    public final void testGetEncoding_SMITH_to_SMTH() { // validated test
        assertEquals("SMTH", this.getStringEncoder().encode("Smith")); // calls a and b
    }

    @Test
    public final void testGetEncoding_SMYTH_to_SMYTH() { // invalidated test
        assertEquals("SMYTH", this.getStringEncoder().encode("Smyth")); // calls a and b
    }

}
