public class MatchRatingApproachEncoder {
    /**
     * Removes accented letters and replaces with non-accented ascii equivalent Case is preserved.
     * http://www.codecodex.com/wiki/Remove_accent_from_letters_%28ex_.%C3%A9_to_e%29
     *
     * @param accentedWord
     *            The word that may have accents in it.
     * @return De-accented word
     */
    String removeAccents(final String accentedWord) { // definition of a
        if (accentedWord == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        final int n = accentedWord.length();

        for (int i = 0; i < n; i++) {
            final char c = accentedWord.charAt(i);
            final int pos = UNICODE.indexOf(c);
            if (pos > -1) {
                sb.append(PLAIN_ASCII.charAt(pos));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
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
    boolean isVowel(final String letter) { // definition of b
        return letter.equalsIgnoreCase("E") || letter.equalsIgnoreCase("A") || letter.equalsIgnoreCase("O") ||
               letter.equalsIgnoreCase("I") || letter.equalsIgnoreCase("U");
    }

    @Override
    public final String encode(String name) { // called from test
        // Bulletproof for trivial input - NINO
        if (name == null || EMPTY.equalsIgnoreCase(name) || SPACE.equalsIgnoreCase(name) || name.length() == 1) {
            return EMPTY;
        }

        // Preprocessing
        name = cleanName(name); // calls a

        // BEGIN: Actual encoding part of the algorithm...
        // 1. Delete all vowels unless the vowel begins the word
        name = removeVowels(name); // calls b

        // 2. Remove second consonant from any double consonant
        name = removeDoubleConsonants(name);

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name);

        return name;
    }

    String cleanName(final String name) {
        String upperName = name.toUpperCase(Locale.ENGLISH);
        ...
        upperName = removeAccents(upperName); // call to a
        upperName = upperName.replaceAll("\\s+", EMPTY);

        return upperName;
    }

    String removeVowels(String name) {
        // Extract first letter
        final String firstLetter = name.substring(0, 1);
        ...
        // return isVowel(firstLetter) ? (firstLetter + name) : name;
        if (isVowel(firstLetter)) { // call to b
            return firstLetter + name;
        }
        return name;
    }

}

public class MatchRatingApproachEncoderTest {
    @Test
    public final void testGetEncoding_SMITH_to_SMTH() {
        assertEquals("SMTH", this.getStringEncoder().encode("Smith"));
    }
}
