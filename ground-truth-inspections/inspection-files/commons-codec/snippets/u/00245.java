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
        name = cleanName(name); // calls b

        // BEGIN: Actual encoding part of the algorithm...
        // 1. Delete all vowels unless the vowel begins the word
        name = removeVowels(name);

        // 2. Remove second consonant from any double consonant
        name = removeDoubleConsonants(name);

        // 3. Reduce codex to 6 letters by joining the first 3 and last 3 letters
        name = getFirst3Last3(name);

        return name;
    }

    String cleanName(final String name) { // called from a
        String upperName = name.toUpperCase(Locale.ENGLISH);

        final String[] charsToTrim = { "\\-", "[&]", "\\'", "\\.", "[\\,]" };
        for (final String str : charsToTrim) {
            upperName = upperName.replaceAll(str, EMPTY);
        }

        upperName = removeAccents(upperName); // call to b
        upperName = upperName.replaceAll("\\s+", EMPTY);

        return upperName;
    }

    /**
     * Removes accented letters and replaces with non-accented ascii equivalent Case is preserved.
     * http://www.codecodex.com/wiki/Remove_accent_from_letters_%28ex_.%C3%A9_to_e%29
     *
     * @param accentedWord
     *            The word that may have accents in it.
     * @return De-accented word
     */
    String removeAccents(final String accentedWord) { // definition of b
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


}

public class MatchRatingApproachEncoderTest {

    @Test
    public final void testGetEncoding_HARPER_HRPR() { // invalidated test
        assertEquals("HRPR", this.getStringEncoder().encode("HARPER")); // call to a
    }

}
