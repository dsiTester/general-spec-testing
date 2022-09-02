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
        name = removeDoubleConsonants(name);

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name); // call to b

        return name;
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
}

public class MatchRatingApproachEncoderTest {

    @Test
    public final void testGetEncoding_HARPER_HRPR() { // invalidated test
        assertEquals("HRPR", this.getStringEncoder().encode("HARPER")); // call to a
    }

}
