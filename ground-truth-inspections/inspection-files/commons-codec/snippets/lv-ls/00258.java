public class MatchRatingApproachEncoder {

    /**
     * Deletes all vowels unless the vowel begins the word.
     *
     * <h2>API Usage</h2>
     * <p>
     * Consider this method private, it is package protected for unit testing only.
     * </p>
     *
     * @param name
     *            The name to have vowels removed
     * @return De-voweled word
     */
    String removeVowels(String name) { // definition of a
        // Extract first letter
        final String firstLetter = name.substring(0, 1);

        name = name.replace("A", EMPTY);
        name = name.replace("E", EMPTY);
        name = name.replace("I", EMPTY);
        name = name.replace("O", EMPTY);
        name = name.replace("U", EMPTY);

        name = name.replaceAll("\\s{2,}\\b", SPACE);

        // return isVowel(firstLetter) ? (firstLetter + name) : name;
        if (isVowel(firstLetter)) {
            return firstLetter + name;
        }
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


    @Override
    public final String encode(String name) { // called from tests
        // Bulletproof for trivial input - NINO
        if (name == null || EMPTY.equalsIgnoreCase(name) || SPACE.equalsIgnoreCase(name) || name.length() == 1) {
            return EMPTY;
        }

        // Preprocessing
        name = cleanName(name);

        // BEGIN: Actual encoding part of the algorithm...
        // 1. Delete all vowels unless the vowel begins the word
        name = removeVowels(name); // call to a

        // 2. Remove second consonant from any double consonant
        name = removeDoubleConsonants(name);

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name); // call to b

        return name;
    }
}

public class MatchRatingApproachEncoderTest {

    @Test
    public final void testGetEncoding_HARPER_HRPR() { // validated test
        assertEquals("HRPR", this.getStringEncoder().encode("HARPER")); // calls a and b
    }

    @Test
    public final void testGetEncoding_SMYTH_to_SMYTH() {
        assertEquals("SMYTH", this.getStringEncoder().encode("Smyth"));
    }

}
