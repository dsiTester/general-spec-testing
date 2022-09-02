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
        if (isVowel(firstLetter)) { // call to b
            return firstLetter + name;
        }
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
        name = removeVowels(name); // call to a

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
        assertEquals("SMTH", this.getStringEncoder().encode("Smith")); // assertion failed here
    }

    @Test
    public final void testGetEncoding_SMYTH_to_SMYTH() { // invalidated test
        assertEquals("SMYTH", this.getStringEncoder().encode("Smyth"));
    }
}
