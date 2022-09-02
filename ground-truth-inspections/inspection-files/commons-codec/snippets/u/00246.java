public class MatchRatingApproachEncoder {
    /**
     * Encodes a String using the Match Rating Approach (MRA) algorithm.
     *
     * @param name
     *            String object to encode
     * @return The MRA code corresponding to the String supplied
     */
    @Override
    public final String encode(String name) { // definition of a
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
        name = removeDoubleConsonants(name); // call to b

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name);

        return name;
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

}

public class MatchRatingApproachEncoderTest {

    @Test
    public final void testGetEncoding_HARPER_HRPR() { // invalidated test
        assertEquals("HRPR", this.getStringEncoder().encode("HARPER")); // call to a
    }

}
