public class MatchRatingApproachEncoder {

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
    String removeDoubleConsonants(final String name) { // definition of a
        String replacedName = name.toUpperCase(Locale.ENGLISH);
        for (final String dc : DOUBLE_CONSONANT) {
            if (replacedName.contains(dc)) {
                final String singleLetter = dc.substring(0, 1);
                replacedName = replacedName.replace(dc, singleLetter);
            }
        }
        return replacedName;
    }

    /**
     * Gets the first and last 3 letters of a name (if &gt; 6 characters) Else just returns the name.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            The string to get the substrings from
     * @return Annexed first and last 3 letters of input word.
     */
    String getFirst3Last3(final String name) { // definition of b
        final int nameLength = name.length();

        if (nameLength > SIX) {
            final String firstThree = name.substring(0, THREE);
            final String lastThree = name.substring(nameLength - THREE, nameLength);
            return firstThree + lastThree;
        }
        return name;
    }

    @Override
    public final String encode(String name) { // called from test
        // Bulletproof for trivial input - NINO
        if (name == null || EMPTY.equalsIgnoreCase(name) || SPACE.equalsIgnoreCase(name) || name.length() == 1) {
            return EMPTY;
        }

        // Preprocessing
        name = cleanName(name);

        // BEGIN: Actual encoding part of the algorithm...
        // 1. Delete all vowels unless the vowel begins the word
        name = removeVowels(name);

        // 2. Remove second consonant from any double consonant
        name = removeDoubleConsonants(name); // call to a

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name); // call to b

        return name;
    }

}

public class MatchRatingApproachEncoderTest {
    @Test
    public final void testGetEncoding_SMITH_to_SMTH() {
        assertEquals("SMTH", this.getStringEncoder().encode("Smith"));
    }
}
